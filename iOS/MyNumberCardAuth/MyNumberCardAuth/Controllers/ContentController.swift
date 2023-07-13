//
//  ContentController.swift
//  MyNumberCardAuth
//
//  Created by abelstaff on 2023/07/04.
//

import Foundation
import SwiftUI


public class ContentController: ObservableObject {
    @Published var urlComponents: URLComponents?
    @Published var queryDict : [String: String]?
    @Published var viewMode: ShowView = .UserVerificationView
    
}
