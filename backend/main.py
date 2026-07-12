import os
import subprocess
from fastapi import FastAPI, HTTPException, Body
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional, Dict, Any
import psutil
from google import genai
from google.genai import types

# Import configuration
import config

app = FastAPI(title="Jarvis Personal Assistant API")

# Setup CORS so the frontend can communicate with the backend
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # In production, specify frontend URL
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# --- SYSTEM UTILITIES ---

def open_application(app_name: str) -> str:
    """Opens a macOS application by name. Example app_name: 'Google Chrome', 'Safari', 'Spotify', 'Calculator', 'Notes'."""
    # Clean app name to prevent shell injection
    safe_app = "".join(c for c in app_name if c.isalnum() or c in " -")
    try:
        subprocess.run(["open", "-a", safe_app], check=True)
        return f"Successfully opened {safe_app}."
    except Exception as e:
        return f"Failed to open {safe_app}: {str(e)}"

def set_system_volume(level: int) -> str:
    """Sets the macOS system volume level (0 to 100)."""
    level = max(0, min(100, int(level)))
    try:
        subprocess.run(["osascript", "-e", f"set volume output volume {level}"], check=True)
        return f"Volume set to {level}%."
    except Exception as e:
        return f"Failed to set volume: {str(e)}"

def get_system_stats() -> dict:
    """Returns the current CPU usage percentage, memory usage percentage, and disk usage percentage of the system."""
    cpu = psutil.cpu_percent(interval=0.1)
    memory = psutil.virtual_memory().percent
    disk = psutil.disk_usage('/').percent
    return {
        "cpu_percent": cpu,
        "memory_percent": memory,
        "disk_percent": disk
    }

def control_media(action: str) -> str:
    """Controls media playback on macOS. action can be 'play', 'pause', 'next', 'previous'."""
    if action not in ["play", "pause", "next", "previous"]:
        return "Invalid action. Use 'play', 'pause', 'next', or 'previous'."
    try:
        if action == "play" or action == "pause":
            script = 'tell application "Spotify" to playpause'
        elif action == "next":
            script = 'tell application "Spotify" to next track'
        elif action == "previous":
            script = 'tell application "Spotify" to previous track'
        
        res = subprocess.run(["osascript", "-e", script], capture_output=True, text=True)
        if res.returncode != 0:
            # Fallback to Music app
            if action == "play" or action == "pause":
                script = 'tell application "Music" to playpause'
            elif action == "next":
                script = 'tell application "Music" to next track'
            elif action == "previous":
                script = 'tell application "Music" to previous track'
            subprocess.run(["osascript", "-e", script])
        return f"Media command '{action}' executed."
    except Exception as e:
        return f"Failed to execute media command: {str(e)}"

# Map of tool names to actual functions for execution
FUNCTION_MAP = {
    "open_application": open_application,
    "set_system_volume": set_system_volume,
    "get_system_stats": get_system_stats,
    "control_media": control_media
}

# --- ENDPOINTS ---

class ChatRequest(BaseModel):
    message: str
    history: Optional[List[Dict[str, Any]]] = None

@app.get("/api/status")
def get_status():
    """Checks system status and whether API key is configured."""
    has_key = len(config.GEMINI_API_KEY.strip()) > 0 or len(os.getenv("GEMINI_API_KEY", "").strip()) > 0
    stats = {}
    try:
        stats = get_system_stats()
    except Exception:
        stats = {"cpu_percent": 0, "memory_percent": 0, "disk_percent": 0}
        
    return {
        "status": "online",
        "api_key_configured": has_key,
        "system_stats": stats
    }

@app.post("/api/config")
def save_config(data: Dict[str, str] = Body(...)):
    """Saves the Gemini API Key to local .env file."""
    api_key = data.get("api_key", "").strip()
    if not api_key:
        raise HTTPException(status_code=400, detail="API key is required.")
        
    try:
        # Write/Update the .env file in the backend directory
        env_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), ".env")
        with open(env_path, "w") as f:
            f.write(f"GEMINI_API_KEY={api_key}\n")
            
        # Update run-time configuration
        config.GEMINI_API_KEY = api_key
        os.environ["GEMINI_API_KEY"] = api_key
        
        return {"message": "API Key saved successfully."}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to save configuration: {str(e)}")

@app.post("/api/chat")
def chat_endpoint(payload: ChatRequest):
    """Sends user message to Gemini, processes any tool calls, and returns the response."""
    # Retrieve current API Key
    api_key = config.GEMINI_API_KEY or os.getenv("GEMINI_API_KEY", "")
    if not api_key:
        raise HTTPException(status_code=400, detail="Gemini API Key is not configured. Please configure it first.")
        
    try:
        client = genai.Client(api_key=api_key)
        
        # Tools list for Gemini model
        tools = [open_application, set_system_volume, get_system_stats, control_media]
        
        config_gen = types.GenerateContentConfig(
            tools=tools,
            system_instruction=(
                "Você é o Jarvis, um assistente pessoal inteligente rodando localmente no macOS do usuário. "
                "Responda sempre em português. Seja prestativo, educado e execute as ações do sistema solicitadas de forma ágil. "
                "Se o usuário pedir para fazer algo no sistema que corresponda a uma ferramenta disponível, use-a. "
                "Após executar a ferramenta, responda confirmando o resultado."
            )
        )
        
        contents = []
        
        # If user history is provided, we can convert it into SDK contents formats
        # Format from frontend: [{"role": "user"/"model", "text": "..."}]
        if payload.history:
            for turn in payload.history:
                role = "user" if turn.get("role") == "user" else "model"
                contents.append(types.Content(role=role, parts=[types.Part.from_text(text=turn.get("text", ""))]))
                
        # Append the new message
        contents.append(types.Content(role="user", parts=[types.Part.from_text(text=payload.message)]))
        
        actions_taken = []
        
        # Multi-turn execution loop (in case tool results prompt more tool calls)
        for _ in range(3):
            response = client.models.generate_content(
                model="gemini-2.5-flash",
                contents=contents,
                config=config_gen
            )
            
            # If no function calls are requested, we are done
            if not response.function_calls:
                return {
                    "text": response.text,
                    "actions": actions_taken
                }
                
            # If there are function calls, we append the model's turn to the conversation history
            contents.append(response.candidates[0].content)
            
            tool_response_parts = []
            for call in response.function_calls:
                func_name = call.name
                func_args = call.args
                
                # Log action
                actions_taken.append({
                    "name": func_name,
                    "args": func_args
                })
                
                # Execute local python function
                if func_name in FUNCTION_MAP:
                    try:
                        result = FUNCTION_MAP[func_name](**func_args)
                    except Exception as e:
                        result = f"Error: {str(e)}"
                else:
                    result = f"Function {func_name} is not implemented."
                    
                # Format response as a tool response part
                tool_response_parts.append(
                    types.Part(
                        function_response=types.FunctionResponse(
                            name=func_name,
                            response={"result": result}
                        )
                    )
                )
                
            # Append the tool execution results back as user input
            contents.append(types.Content(role="user", parts=tool_response_parts))
            
        return {
            "text": response.text or "Desculpe, não consegui concluir a requisição.",
            "actions": actions_taken
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Gemini API Error: {str(e)}")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host=config.HOST, port=config.PORT)
