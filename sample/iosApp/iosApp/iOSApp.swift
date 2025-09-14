import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
	var body: some Scene {
		WindowGroup {
			ContentView()
				.onOpenURL { url in

					// Handle supabase deep link url
					//If using only kmauth-apple
					KMAuthApple.shared.deepLinkHandler().handleDeepLinks(url: url)

					//If using kmauth-supabase directly
					//KMAuthSupabase.shared.deepLinkHandler().handleDeepLinks(url: url)
				}
		}
	}
}