//
//  AppExplanationView.swift
//  MyNumberCardAuth
//
//  Created by c3lab on 2023/07/10.
//

import SwiftUI

struct AppExplanationView: View {
    @ObservedObject var authenticationController: AuthenticationController

    var body: some View {
        VStack {
            Text("About this app")
                .font(.title)
                .multilineTextAlignment(.center)
                .bold()
                .padding(/*@START_MENU_TOKEN@*/ .all/*@END_MENU_TOKEN@*/)

            Text("It is an application for logging in using a browser. Start it from your browser and use it.")
                .font(.title3)
                .multilineTextAlignment(.center)
                .padding(/*@START_MENU_TOKEN@*/ .all/*@END_MENU_TOKEN@*/)
                .frame(height: 120.0)

            Button("Terms Of Service") {
                self.authenticationController.openURL(string: authenticationController.termsOfUseURL)

            }.modifier(SmallButtonModifier(color: self.authenticationController.getButtonColor(checkStr: self.authenticationController.termsOfUseURL)))

            Button("Privacy Policy") {
                self.authenticationController.openURL(string: self.authenticationController.privacyPolicyURL)

            }.modifier(SmallButtonModifier(color: self.authenticationController.getButtonColor(checkStr: self.authenticationController.privacyPolicyURL)))

            Button("Personal Data Protection Policy") {
                self.authenticationController.openURL(string: self.authenticationController.protectionPolicyURL)

            }.modifier(SmallButtonModifier(color: self.authenticationController.getButtonColor(checkStr: self.authenticationController.protectionPolicyURL)))
        }
    }
}

struct AppExplanationView_Previews: PreviewProvider {
    @ObservedObject static var authenticationController: AuthenticationController = .init()

    static var previews: some View {
        AppExplanationView(authenticationController: authenticationController)
    }
}
