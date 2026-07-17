import CarPlay
import Combine
import UIKit

@MainActor
final class CarPlaySceneDelegate: UIResponder, CPTemplateApplicationSceneDelegate, CPInterfaceControllerDelegate {
    private var interfaceController: CPInterfaceController?
    private var cancellables = Set<AnyCancellable>()
    private let session = JarvisSession.shared

    private var isPushingVoiceControl = false
    private var isPoppingToRoot = false
    private var voiceControlTemplateIsOnStack = false

    private let listItem = CPListItem(text: "Diga “Ei Jarvis”", detailText: "Toque para ativar o assistente por voz")

    private lazy var listTemplate: CPListTemplate = {
        listItem.handler = { [weak self] _, completion in
            self?.activateJarvis()
            completion()
        }
        let section = CPListSection(items: [listItem])
        return CPListTemplate(title: "Jarvis", sections: [section])
    }()

    private lazy var voiceControlTemplate: CPVoiceControlTemplate = {
        let states = [
            CPVoiceControlState(identifier: "idle", titleVariants: ["Diga “Ei Jarvis”"], image: UIImage(systemName: "mic"), repeats: false),
            CPVoiceControlState(identifier: "listening", titleVariants: ["Ouvindo…"], image: UIImage(systemName: "waveform"), repeats: true),
            CPVoiceControlState(identifier: "thinking", titleVariants: ["Pensando…"], image: UIImage(systemName: "ellipsis.circle"), repeats: true),
            CPVoiceControlState(identifier: "speaking", titleVariants: ["Falando…"], image: UIImage(systemName: "speaker.wave.2"), repeats: true),
            CPVoiceControlState(identifier: "error", titleVariants: ["Algo falhou. Volte e tente de novo."], image: UIImage(systemName: "exclamationmark.triangle"), repeats: false)
        ]
        return CPVoiceControlTemplate(voiceControlStates: states)
    }()

    func templateApplicationScene(
        _ templateApplicationScene: CPTemplateApplicationScene,
        didConnect interfaceController: CPInterfaceController
    ) {
        self.interfaceController = interfaceController
        interfaceController.delegate = self
        interfaceController.setRootTemplate(listTemplate, animated: false) { [weak self] _, _ in
            guard let self else { return }
            self.observeSession()
            if self.session.isActivated {
                self.presentVoiceControl()
            }
        }
    }

    func templateApplicationScene(
        _ templateApplicationScene: CPTemplateApplicationScene,
        didDisconnectInterfaceController interfaceController: CPInterfaceController
    ) {
        cancellables.removeAll()
        isPushingVoiceControl = false
        isPoppingToRoot = false
        voiceControlTemplateIsOnStack = false
        self.interfaceController = nil
    }

    private func activateJarvis() {
        guard JarvisSpeechRecognizer.isAuthorized else {
            presentPermissionAlert()
            return
        }
        presentVoiceControl()
        session.start()
    }

    func templateDidDisappear(_ aTemplate: CPTemplate, animated: Bool) {
        guard aTemplate === voiceControlTemplate else { return }
        voiceControlTemplateIsOnStack = false
        if session.isActivated {
            session.stop()
        }
    }

    private func presentVoiceControl() {
        guard !isPushingVoiceControl, interfaceController?.topTemplate !== voiceControlTemplate else { return }
        isPushingVoiceControl = true
        interfaceController?.pushTemplate(voiceControlTemplate, animated: true) { [weak self] _, _ in
            guard let self else { return }
            self.isPushingVoiceControl = false
            self.voiceControlTemplateIsOnStack = true
            self.updateVoiceControlState(for: self.session.state)
        }
    }

    private func popToRootIfNeeded() {
        guard !isPoppingToRoot, interfaceController?.topTemplate === voiceControlTemplate else { return }
        isPoppingToRoot = true
        interfaceController?.popToRootTemplate(animated: true) { [weak self] _, _ in
            guard let self else { return }
            self.isPoppingToRoot = false
            self.voiceControlTemplateIsOnStack = false
        }
    }

    private func presentPermissionAlert() {
        let action = CPAlertAction(title: "OK", style: .default) { [weak self] _ in
            self?.interfaceController?.dismissTemplate(animated: true, completion: nil)
        }
        let alert = CPAlertTemplate(
            titleVariants: ["Abra o Jarvis no iPhone primeiro para conceder acesso ao microfone e à fala."],
            actions: [action]
        )
        interfaceController?.presentTemplate(alert, animated: true, completion: nil)
    }

    private func observeSession() {
        session.$state
            .receive(on: DispatchQueue.main)
            .sink { [weak self] state in
                self?.updateVoiceControlState(for: state)
            }
            .store(in: &cancellables)

        session.$isActivated
            .receive(on: DispatchQueue.main)
            .sink { [weak self] isActivated in
                self?.listItem.setText(isActivated ? "Jarvis ativo" : "Diga “Ei Jarvis”")
                if !isActivated {
                    self?.popToRootIfNeeded()
                }
            }
            .store(in: &cancellables)

        session.$isStarting
            .receive(on: DispatchQueue.main)
            .sink { [weak self] isStarting in
                self?.listItem.setDetailText(isStarting ? "Ativando…" : "Toque para ativar o assistente por voz")
            }
            .store(in: &cancellables)
    }

    private func updateVoiceControlState(for state: JarvisSession.State) {
        guard voiceControlTemplateIsOnStack, session.isActivated else { return }
        switch state {
        case .idle: voiceControlTemplate.activateVoiceControlState(withIdentifier: "idle")
        case .listening: voiceControlTemplate.activateVoiceControlState(withIdentifier: "listening")
        case .thinking: voiceControlTemplate.activateVoiceControlState(withIdentifier: "thinking")
        case .speaking: voiceControlTemplate.activateVoiceControlState(withIdentifier: "speaking")
        case .error: voiceControlTemplate.activateVoiceControlState(withIdentifier: "error")
        }
    }
}
