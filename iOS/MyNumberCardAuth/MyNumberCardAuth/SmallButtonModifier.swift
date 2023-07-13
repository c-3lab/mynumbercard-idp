//
//  SmallButtonModifier.swift
//  MyNumberCardAuth
//
//  Created by abelstaff on 2023/03/29.
//

import SwiftUI

struct SmallButtonModifier: ViewModifier {
    let color: Color
    func body(content: Content) -> some View {
        content
            .frame(width: 200.0)
            .padding(.all)
            .background(color)
            .foregroundColor(.white)
    }
}
