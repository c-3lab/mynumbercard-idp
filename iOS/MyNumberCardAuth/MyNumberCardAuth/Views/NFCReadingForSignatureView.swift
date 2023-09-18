//
//  NFCReadingForSignatureView.swift
//  MyNumberCardAuth
//
//  Created by c3lab on 2023/06/28.
//

import Combine
import SwiftUI

struct NFCReadingForSignatureView: View {
    @ObservedObject var authenticationController: AuthenticationController
    @FocusState private var isActive: Bool
    @ObservedObject var controller: SignatureViewController

    var body: some View {
        VStack {
            Text("My number card \n authentication")
                .font(.title)
                .multilineTextAlignment(.center)
                .bold()
                .padding(/*@START_MENU_TOKEN@*/ .all/*@END_MENU_TOKEN@*/)

            (Text("Enter the password (6 to 16 alphanumeric characters) of the electronic ") + Text("certificate for signature ").fontWeight(.bold) + Text("of My Number Card and press the start reading button "))
                .font(.title3)
                .multilineTextAlignment(.center)
                .padding(/*@START_MENU_TOKEN@*/ .all/*@END_MENU_TOKEN@*/)
                .frame(height: 140.0)

            SecureField("password", text: $controller.inputPIN)
                .padding(.all)
                .frame(height: 80.0)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .focused(self.$isActive)
                .multilineTextAlignment(.center)
                .toolbar {
                    ToolbarItemGroup(placement: .keyboard) {
                        Spacer()
                        Button("close") {
                            isActive = false
                        }
                    }
                }.onReceive(Just(controller.inputPIN)) { _ in
                    // 最大文字数を超えたら、最大文字数までの文字列を代入する
                    if controller.inputPIN.count > 16 {
                        controller.inputPIN = String(controller.inputPIN.prefix(16))
                    }
                }
                .alert(isPresented: $authenticationController.isAlert) {
                    Alert(title: Text(self.authenticationController.messageTitle), message: Text(self.authenticationController.messageString), dismissButton: .default(Text("OK"), action: {
                        if self.authenticationController.isErrorOpenURL {
                            self.authenticationController
                                .openURL(string: self.authenticationController.openURL)
                        }
                    }))
                }

            Button("start reading") {
                if controller.isEnableButton() {
                    self.authenticationController.startReading(pin: controller.inputPIN, nonce: controller.nonce, actionURL: controller.actionURL)
                }
            }.modifier(SmallButtonModifier(color: controller.getButtonColor()))
                .alert(isPresented: self.$authenticationController.isLinkAlert) {
                    Alert(title: Text(self.authenticationController.messageTitle), message: Text(self.authenticationController.messageString), primaryButton: .default(Text("Contact page"), action: {
                        self.authenticationController
                            .openURL(string: self.authenticationController.inquiryURL)
                    }), secondaryButton: .default(Text("OK"), action: {}))
                }
        }
    }
}

struct NFCReadingForSignatureView_Previews: PreviewProvider {
    @ObservedObject static var authenticationController: AuthenticationController = .init()
    @ObservedObject static var signatureController: SignatureViewController = .init()
    static var previews: some View {
        NFCReadingForSignatureView(authenticationController: authenticationController, controller: signatureController)
    }
}
