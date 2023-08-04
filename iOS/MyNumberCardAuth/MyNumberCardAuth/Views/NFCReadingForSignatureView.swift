//
//  NFCReadingForSignatureView.swift
//  MyNumberCardAuth
//
//  Created by c3lab on 2023/06/28.
//

import SwiftUI
import Combine

struct NFCReadingForSignatureView: View {
    @Binding var queryDict : [String: String]?
    @ObservedObject var authenticationController:AuthenticationController
    @FocusState private var isActive:Bool
    @ObservedObject var controller:SignatureViewController
    
    var body: some View {
        VStack{
            Text(NSLocalizedString("my number card authentication", comment: "マイナンバーカード認証"))
                .font(.title)
                .multilineTextAlignment(.center)
                .bold()
                .padding(/*@START_MENU_TOKEN@*/.all/*@END_MENU_TOKEN@*/)
            
            (Text(NSLocalizedString("signature for my namber", comment: "マイナンバーカードの")) + Text(NSLocalizedString("certificate for signature", comment: "署名用電子証明書")).fontWeight(.bold) + Text(NSLocalizedString("press the start signature", comment: "のパスワード（6～16桁の英数字）を入力し、読み取り開始ボタンを押す")))
                .font(.title3)
                .multilineTextAlignment(.center)
                .padding(/*@START_MENU_TOKEN@*/.all/*@END_MENU_TOKEN@*/)
                .frame(height: 140.0)
            
            SecureField(NSLocalizedString("password", comment: "暗証番号"), text: $controller.inputPIN)
                .padding(.all)
                .frame( height: 80.0)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .focused(self.$isActive)
                .multilineTextAlignment(.center)
                .toolbar {
                    ToolbarItemGroup(placement: .keyboard) {
                        Spacer()
                        Button(NSLocalizedString("close", comment: "閉じる")) {
                            isActive = false
                        }
                    }
                }.onReceive(Just(controller.inputPIN)) { _ in
                    //最大文字数を超えたら、最大文字数までの文字列を代入する
                    if controller.inputPIN.count > 16 {
                        controller.inputPIN = String(controller.inputPIN.prefix(16))
                    }
                }
                .alert(isPresented:$authenticationController.isAlert){
                    Alert(title:Text(self.authenticationController.messageTitle),message:Text(self.authenticationController.messageString),dismissButton: .default(Text("OK"),action: {
                            if(self.authenticationController.isErrorOpenURL == true){
                                if (self.authenticationController.openURL.isEmpty == false)
                                {
                                    if let openURL = URL(string:self.authenticationController.openURL){
                                        UIApplication.shared.open(openURL)
                                    };
                                }
                            }
                    }))
                }
            
            Button(NSLocalizedString("start reading", comment: "読み取り開始")) {
                if controller.isEnableButton() {
                    self.authenticationController.startReading(pin: controller.inputPIN, nonce: controller.nonce ,actionURL: controller.actionURL)
                }
            }.modifier(SmallButtonModifier(color: controller.getButtonColor()))
            .alert(isPresented:self.$authenticationController.isLinkAlert){
                    Alert(title:Text(self.authenticationController.messageTitle),message:Text(self.authenticationController.messageString),primaryButton: .default(Text(NSLocalizedString("contact page", comment: "問い合わせページ")),action: {
                        if (self.authenticationController.inquiryURL.isEmpty == false)
                        {
                            if let openURL = URL(string:self.authenticationController.inquiryURL){
                                UIApplication.shared.open(openURL)
                            };
                        }
                    }),secondaryButton: .default(Text("OK"),action: {})
                    )}
        }
    }
}

struct NFCReadingForSignatureView_Previews: PreviewProvider {
    @ObservedObject static var authenticationController:AuthenticationController = AuthenticationController()
    @ObservedObject static var signatureController:SignatureViewController = SignatureViewController()
    static var previews: some View {
        NFCReadingForSignatureView(queryDict: .constant([:]),authenticationController: authenticationController,controller: signatureController)
    }
}
