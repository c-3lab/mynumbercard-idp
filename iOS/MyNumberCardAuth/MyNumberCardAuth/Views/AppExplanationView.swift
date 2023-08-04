//
//  AppExplanationView.swift
//  MyNumberCardAuth
//
//  Created by c3lab on 2023/07/10.
//

import SwiftUI

struct AppExplanationView: View {
    @ObservedObject var authenticationController:AuthenticationController
    
    var body: some View {
        VStack{
            Text(NSLocalizedString("app about", comment: "このアプリについて"))
                .font(.title)
                .multilineTextAlignment(.center)
                .bold()
                .padding(/*@START_MENU_TOKEN@*/.all/*@END_MENU_TOKEN@*/)
            
            Text(NSLocalizedString("use browser", comment: "ブラウザを使用してログインするためのアプリです。ブラウザから起動して使用してください。"))
                .font(.title3)
                .multilineTextAlignment(.center)
                .padding(/*@START_MENU_TOKEN@*/.all/*@END_MENU_TOKEN@*/)
                .frame(height: 120.0)
            
            Button(NSLocalizedString("terms of service", comment: "利用規約")) {
                self.authenticationController.openURLButton(url: authenticationController.termsOfUseURL)

            }.modifier(SmallButtonModifier(color: self.authenticationController.getButtonColor(checkStr: self.authenticationController.termsOfUseURL)))
            
            Button(NSLocalizedString("privacy policy", comment: "プライバシーポリシー")) {
                self.authenticationController.openURLButton(url:self.authenticationController.privacyPolicyURL)
                
            }.modifier(SmallButtonModifier(color: self.authenticationController.getButtonColor(checkStr: self.authenticationController.privacyPolicyURL)))

            Button(NSLocalizedString("personal data protection policy", comment: "個人情報保護方針")) {
                
                self.authenticationController.openURLButton(url:self.authenticationController.protectionPolicyURL)
                
            }.modifier(SmallButtonModifier(color: self.authenticationController.getButtonColor(checkStr: self.authenticationController.protectionPolicyURL)))
        }
    }
}

struct AppExplanationView_Previews: PreviewProvider {
    @ObservedObject static var authenticationController:AuthenticationController = AuthenticationController()
    
    static var previews: some View {
        AppExplanationView(authenticationController: authenticationController)
    }
}
