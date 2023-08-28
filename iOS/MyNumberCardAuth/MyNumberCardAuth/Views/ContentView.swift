//
//  ContentView.swift
//  MyNumberCardAuth
//
//  Created by c3lab on 2023/03/24.
//

import SwiftUI

struct ContentView: View {
    @Binding var urlComponents: URLComponents?
    @Binding var queryDict : [String: String]?
    @ObservedObject var authenticationController:AuthenticationController
    @ObservedObject var controller:ViewController
    @ObservedObject var controllerForSignature:SignatureViewController
    @State private var toNfcReadingView = false
    
    var body: some View {
        NavigationView {
            switch authenticationController.viewState {
            case .UserVerificationView:
                // 利用者証明用電子証明書読込を表示
                NFCReadingView(queryDict: $queryDict,authenticationController: self.authenticationController,controller: self.controller)
                    .toolbar {
                      ToolbarItem(placement: .principal) {
                        Text("[iOS] MyNumberCard Authorization App")
                              .bold()
                              .multilineTextAlignment(.center)
                              .padding(/*@START_MENU_TOKEN@*/.all/*@END_MENU_TOKEN@*/)
                              .minimumScaleFactor(0.8)
                      }
                    }
                    
            case .SignatureView:
                // 署名用電子証明書読込を表示
                NFCReadingForSignatureView(queryDict: $queryDict, authenticationController: self.authenticationController,controller: self.controllerForSignature)
                    .toolbar {
                      ToolbarItem(placement: .principal) {
                        Text("[iOS] MyNumberCard Authorization App")
                              .bold()
                              .multilineTextAlignment(.center)
                              .padding(/*@START_MENU_TOKEN@*/.all/*@END_MENU_TOKEN@*/)
                              .minimumScaleFactor(0.8)
                      }
                    }
                
            case .ExplanationView:
                // アプリ説明画面
                AppExplanationView(authenticationController: self.authenticationController)
                    .toolbar {
                      ToolbarItem(placement: .principal) {
                        Text("[iOS] MyNumberCard Authorization App")
                              .bold()
                              .multilineTextAlignment(.center)
                              .padding(/*@START_MENU_TOKEN@*/.all/*@END_MENU_TOKEN@*/)
                              .minimumScaleFactor(0.8)
                      }
                    }
            }
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    @ObservedObject static var authenticationController:AuthenticationController = AuthenticationController()
    @ObservedObject static var controlller:ViewController = ViewController()
    @ObservedObject static var signatureController:SignatureViewController = SignatureViewController()
    static var previews: some View {
        ContentView(urlComponents: .constant(nil), queryDict: .constant([:]),authenticationController: authenticationController,controller: controlller,controllerForSignature: signatureController)
    }
}
