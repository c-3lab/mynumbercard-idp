//
//  AlertController.swift
//  MyNumberCardAuth
//
//  Created by abelstaff on 2023/07/03.
//

import SwiftUI

struct AlertController: UIViewControllerRepresentable {
    //@Binding var isPresented: Bool
    //@Binding var text: String

    let title: String?
    let message: String?
    let placeholderText: String
    
    class Coordinator: NSObject {
        var alert: UIAlertController!
        
        @objc func textDidChange(_ sender: UITextField) {
            if sender.text!.count == 0 {
                alert.actions[1].isEnabled = false
            } else {
                alert.actions[1].isEnabled = true
            }
        }
    }

    func makeCoordinator() -> AlertController.Coordinator {
        return Coordinator()
    }

    func makeUIViewController(context: Context) -> UIViewController {
        return UIViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        if true {
            let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)
            
            context.coordinator.alert = alert
            // TextFieldの追加
            /*alert.addTextField { textField in
                textField.placeholder = placeholderText
                textField.returnKeyType = .done
                textField.addTarget(context.coordinator, action: #selector(Coordinator.textDidChange(_:)), for: .editingChanged)
            }
            
            // キャンセルボタンアクション
            let cancelAction = UIAlertAction(title: "キャンセル", style: .cancel) {_ in }
            
            // OKボタンアクション
            let okAction = UIAlertAction(title: "OK", style: .default) { _ in
                let textField = alert.textFields!.first!
                //self.text = Text("OK")
            }
            okAction.isEnabled = false
            
            alert.addAction(cancelAction)
            alert.addAction(okAction)
            */
            uiViewController.present(alert, animated: true) {
                //isPresented = false
            }
        }
    }
}
