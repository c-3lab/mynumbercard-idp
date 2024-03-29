//
//  HTTPSession.swift
//  MyNumberCardAuth
//
//  Created by c3lab on 2023/04/20.
//

import Foundation
import UIKit

public class HTTPSession: NSObject, URLSessionDelegate, URLSessionTaskDelegate {
    private var authenticationController: AuthenticationControllerProtocol
    private let makeURLSession: (_ configuration: URLSessionConfiguration, _ delegate: URLSessionDelegate?, _ delegateQueue: OperationQueue?) -> URLSessionProtocol

    convenience init(authenticationController: AuthenticationControllerProtocol) {
        self.init(authenticationController: authenticationController, makeURLSession: {
            URLSession(configuration: $0, delegate: $1, delegateQueue: $2)
        })
    }

    init(authenticationController: AuthenticationControllerProtocol, makeURLSession: @escaping (_ configuration: URLSessionConfiguration, _ delegate: URLSessionDelegate?, _ delegateQueue: OperationQueue?) -> URLSessionProtocol) {
        self.authenticationController = authenticationController
        self.makeURLSession = makeURLSession
    }

    public func urlSession(_: URLSession, task _: URLSessionTask, willPerformHTTPRedirection _: HTTPURLResponse, newRequest _: URLRequest, completionHandler: @escaping (URLRequest?) -> Void) {
        completionHandler(nil)
    }

    private func getDataFromServerWithSuccess(request: URLRequest, noRedirect: Bool, success: @escaping (_ response: URLResponse?) -> Void) {
        var urlSessionDelegate: URLSessionDelegate? = nil
        if noRedirect {
            urlSessionDelegate = self
        }
        let session = makeURLSession(URLSessionConfiguration.default,
                                     urlSessionDelegate,
                                     nil)
        let task = session.dataTask(with: request) { _, response, error in
            if let error = error {
                print(error)
            }
            success(response)
        }
        task.resume()
    }

    public func openRedirectURLOnSafari(request: URLRequest) {
        getDataFromServerWithSuccess(request: request, noRedirect: true) { response in

            if let httpResponse = response as? HTTPURLResponse {
                print("API result")
                print(httpResponse.statusCode)
                print(httpResponse.allHeaderFields)

                // ステータスコード判定
                if httpResponse.statusCode == 400 ||
                    httpResponse.statusCode == 500 ||
                    httpResponse.statusCode == 503
                {
                    DispatchQueue.main.async {
                        self.authenticationController.isAlert = true
                        self.authenticationController.messageTitle = String(localized: "Failure", comment: "失敗")
                        self.authenticationController.messageString = String(localized: "An unexpected error has occurred.", comment: "予期せぬエラーが発生しました。")
                    }
                }

                if httpResponse.statusCode == 404 {
                    DispatchQueue.main.async {
                        self.authenticationController.isAlert = true
                        self.authenticationController.isErrorOpenURL = true
                        self.authenticationController.messageTitle = String(localized: "Authentication failure", comment: "認証失敗")
                        self.authenticationController.messageString = String(localized: "Since you have not registered as a user, please register as a user.", comment: "ユーザー未登録のため、利用者登録を行ってください。")
                    }
                }

                if httpResponse.statusCode == 401 {
                    DispatchQueue.main.async {
                        self.authenticationController.isLinkAlert = true
                        self.authenticationController.messageTitle = String(localized: "Authentication failure", comment: "認証失敗")
                        if self.authenticationController.runMode == .Login {
                            self.authenticationController.messageString = String(localized: "The electronic certificate for user certification has been revoked.", comment: "利用者証明用電子証明書が失効しています。") + String(localized: "Please contact the window of your municipality, or contact the window of the My Number Card Comprehensive Site from the link below.", comment: "お住まいの市区町村の窓口へお問い合わせください。")
                        } else {
                            self.authenticationController.messageString = String(localized: "The electronic signature certificate has expired.", comment: "署名用電子証明書が失効しています。") + String(localized: "Please contact the window of your municipality, or contact the window of the My Number Card Comprehensive Site from the link below.", comment: "お住まいの市区町村の窓口へお問い合わせください。")
                        }
                    }
                }

                if httpResponse.statusCode == 409 {
                    DispatchQueue.main.async {
                        self.authenticationController.isAlert = true
                        self.authenticationController.isErrorOpenURL = true
                        self.authenticationController.messageTitle = String(localized: "Registration failed", comment: "登録失敗")
                        self.authenticationController.messageString = String(localized: "Since the user is already registered, please log in.", comment: "既にユーザーが登録されているため、ログインを行ってください。")
                    }
                }

                if httpResponse.statusCode == 410 {
                    DispatchQueue.main.async {
                        let keyValues = httpResponse.allHeaderFields.map { (String(describing: $0.key).lowercased(), String(describing: $0.value)) }
                        if let headerValue = keyValues.filter({ $0.0 == String("X-ACTION-URL").lowercased() }).first {
                            self.authenticationController.controllerForSignature.actionURL = headerValue.1
                        }

                        self.authenticationController.viewState = .SignatureView
                        self.authenticationController.runMode = .Replacement
                        self.authenticationController.isAlert = true
                        self.authenticationController.messageTitle = String(localized: "Reloading My Number Card", comment: "マイナンバーカードの再読み込み")
                        self.authenticationController.messageString = String(localized: "I need to change my registration information. Please read the signature digital certificate to make changes.", comment: "登録情報の変更が必要です。変更を行うため署名用電子証明書の読み取りを行ってください。")
                    }
                }

                if let redirectURL = httpResponse.allHeaderFields["Location"] as? String {
                    print("リダイレクト先のURL: \(redirectURL)")
                    DispatchQueue.main.async {
                        self.authenticationController.open(urlString: redirectURL)
                    }
                }
            }
        }
    }
}

extension HTTPSession: HTTPSessionProtocol {}
