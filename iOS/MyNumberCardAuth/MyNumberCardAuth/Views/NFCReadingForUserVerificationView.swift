//
//  NFCReadingForUserVerificationView.swift
//  MyNumberCardAuth
//
//  Created by c3lab on 2023/04/20.
//

import Combine
import SwiftUI

struct NFCReadingForUserVerificationView: View {
    @ObservedObject var authenticationController: AuthenticationController
    @FocusState private var isActive: Bool
    @ObservedObject var controller: UserVerificationViewController

    var body: some View {
        VStack {
            Text("My number card \n authentication")
                .font(.title)
                .multilineTextAlignment(.center)
                .bold()
                .padding(/*@START_MENU_TOKEN@*/ .all/*@END_MENU_TOKEN@*/)

            (Text("Enter the password (4-digit number) of the electronic ") + Text("certificate for user verification ").fontWeight(.bold) + Text("of My Number Card and press the start reading button. ")
            ).font(.title3)
                .multilineTextAlignment(.center)
                .padding(/*@START_MENU_TOKEN@*/ .all/*@END_MENU_TOKEN@*/)
                .frame(height: 140.0)

            SecureField("password", text: $controller.inputPIN)
                .padding(.all)
                .frame(height: 80.0)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .keyboardType(.numberPad)
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
                    if controller.inputPIN.count > 4 {
                        controller.inputPIN = String(controller.inputPIN.prefix(4))
                    }
                }
                .alert(isPresented: self.$authenticationController.isAlert) {
                    Alert(title: Text(self.authenticationController.messageTitle), message: Text(self.authenticationController.messageString), dismissButton: .default(Text("OK"), action: {
                        if self.authenticationController.isErrorOpenURL {
                            self.authenticationController.open(urlString: self.authenticationController.openURL)
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
                            .open(urlString: self.authenticationController.inquiryURL)
                    }), secondaryButton: .default(Text("OK"), action: {}))
                }
        }
        .onAppear {
            if self.controller.actionURL.isEmpty || self.controller.nonce.isEmpty {
                // アプリ単独起動時
                self.authenticationController.viewState = .ExplanationView
            }
        }
    }
}

struct NFCReadingView_Previews: PreviewProvider {
    @ObservedObject static var authenticationController: AuthenticationController = .init()
    @ObservedObject static var controlller: UserVerificationViewController = .init()
    static var previews: some View {
        NFCReadingForUserVerificationView(authenticationController: authenticationController, controller: controlller)
    }
}
