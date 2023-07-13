//
//  AppExplanationView.swift
//  MyNumberCardAuth
//
//  Created by abelstaff on 2023/07/10.
//

import SwiftUI

struct AppExplanationView: View {
    @ObservedObject var authenticationController:AuthenticationController
    
    var body: some View {
        VStack{
            Text(NSLocalizedString("m03-001", comment: "このアプリについて"))
                .font(.title)
                .multilineTextAlignment(.center)
                .bold()
                .padding(/*@START_MENU_TOKEN@*/.all/*@END_MENU_TOKEN@*/)
            
            Text(NSLocalizedString("m03-002", comment: "ブラウザを使用してログインするためのアプリです。ブラウザから起動して使用してください。"))
                .font(.title3)
                .multilineTextAlignment(.center)
                .padding(/*@START_MENU_TOKEN@*/.all/*@END_MENU_TOKEN@*/)
                .frame(height: 120.0)
            
            Button(NSLocalizedString("m03-003", comment: "利用規約")) {
                if self.authenticationController.termsOfUseURL.isEmpty == false {
                    if let openURL = URL(string:self.authenticationController.termsOfUseURL)
                    {
                        UIApplication.shared.open(openURL)
                    }
                    
                }
            }.modifier(SmallButtonModifier(color: self.authenticationController.toggleColor(url: self.authenticationController.termsOfUseURL)))
            
            Button(NSLocalizedString("m03-004", comment: "プライバシーポリシー")) {
                if self.authenticationController.privacyPolicyURL.isEmpty == false {
                    if let openURL = URL(string:self.authenticationController.privacyPolicyURL)
                    {
                        UIApplication.shared.open(openURL)
                    }
                    
                }
            }.modifier(SmallButtonModifier(color: self.authenticationController.toggleColor(url: self.authenticationController.privacyPolicyURL)))

            Button(NSLocalizedString("m03-005", comment: "個人情報保護方針")) {
                if self.authenticationController.protectionPolicyURL.isEmpty == false {
                    if let openURL = URL(string:self.authenticationController.protectionPolicyURL)
                    {
                        UIApplication.shared.open(openURL)
                    }
                    
                }
            }.modifier(SmallButtonModifier(color: self.authenticationController.toggleColor(url: self.authenticationController.protectionPolicyURL)))
        }
    }
}

struct AppExplanationView_Previews: PreviewProvider {
    @ObservedObject static var authenticationController:AuthenticationController = AuthenticationController()
    
    static var previews: some View {
        AppExplanationView(authenticationController: authenticationController)
    }
}
