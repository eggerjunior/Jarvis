import CarPlay
import UIKit

final class CarPlaySceneDelegate: UIResponder, CPTemplateApplicationSceneDelegate {
    private var interfaceController: CPInterfaceController?

    func templateApplicationScene(
        _ templateApplicationScene: CPTemplateApplicationScene,
        didConnect interfaceController: CPInterfaceController
    ) {
        self.interfaceController = interfaceController

        let listeningItem = CPListItem(text: "Diga “Ei Jarvis”", detailText: "Toque para ativar o assistente por voz")
        listeningItem.handler = { [weak self] _, completion in
            self?.activateJarvis()
            completion()
        }

        let section = CPListSection(items: [listeningItem])
        let template = CPListTemplate(title: "Jarvis", sections: [section])
        interfaceController.setRootTemplate(template, animated: true, completion: nil)
    }

    func templateApplicationScene(
        _ templateApplicationScene: CPTemplateApplicationScene,
        didDisconnectInterfaceController interfaceController: CPInterfaceController
    ) {
        self.interfaceController = nil
    }

    private func activateJarvis() {
        NotificationCenter.default.post(name: .jarvisCarPlayActivate, object: nil)
    }
}

extension Notification.Name {
    static let jarvisCarPlayActivate = Notification.Name("jarvisCarPlayActivate")
}
