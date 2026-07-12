import os
from dotenv import load_dotenv

# Load environment variables from a .env file if it exists
load_dotenv()

# Configuration settings
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY", "")
PORT = int(os.getenv("PORT", 8000))
HOST = os.getenv("HOST", "0.0.0.0")

# System specific commands can be mapped here
# We'll use osascript to control volume, lock screen, and open apps on Mac.
