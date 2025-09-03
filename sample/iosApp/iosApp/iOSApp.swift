import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
	var body: some Scene {
		WindowGroup {
			ContentView()
				.onOpenURL { url in
					//Handle supabase deep link url
					KMAuthSupabase.shared.deepLinkHandler().handleDeepLinks(url: url)
				}
		}
	}
}