// Test-only native input adapter. Does not load or call Minecraft/mod code.
// Compile: xcrun swiftc tools/gui-validation/macos_shift_click.swift -o build/gui-validation/shift-click
import AppKit
import ApplicationServices

enum InputError: Error, CustomStringConvertible {
    case invalid(String)
    var description: String {
        switch self { case .invalid(let message): return message }
    }
}

func run() throws {
    let args = Array(CommandLine.arguments.dropFirst())
    if args == ["--check"] {
        print("accessibilityTrusted=\(AXIsProcessTrusted())")
        return
    }
    guard args.count == 3, let pid = Int32(args[0]),
          let x = Double(args[1]), let y = Double(args[2]), x.isFinite, y.isFinite else {
        throw InputError.invalid("Usage: shift-click --check | <client-pid> <window-x-points> <window-y-points>")
    }
    guard AXIsProcessTrusted() else {
        throw InputError.invalid("Accessibility permission is unavailable; no events sent. No permission prompt or settings change was requested.")
    }
    let allowed = ["com.river-quinn.ect.gui-dev.fabric.mc1211", "com.river-quinn.ect.gui-dev.mc1211"]
    guard let app = NSRunningApplication(processIdentifier: pid),
          let bundle = app.bundleIdentifier, allowed.contains(bundle) else {
        throw InputError.invalid("Target must be the dedicated Fabric/NeoForge 1.21.1 GUI client.")
    }
    guard let windows = CGWindowListCopyWindowInfo([.optionOnScreenOnly, .excludeDesktopElements], kCGNullWindowID) as? [[String: Any]],
          let window = windows.first(where: {
              guard ($0[kCGWindowOwnerPID as String] as? Int32) == pid,
                    ($0[kCGWindowLayer as String] as? Int) == 0,
                    let raw = $0[kCGWindowBounds as String] as? NSDictionary,
                    let frame = CGRect(dictionaryRepresentation: raw) else { return false }
              return frame.width >= 300 && frame.height >= 200
          }),
          let bounds = window[kCGWindowBounds as String] as? NSDictionary,
          let rectangle = CGRect(dictionaryRepresentation: bounds) else {
        throw InputError.invalid("No on-screen client window found.")
    }
    guard x >= 0, y >= 24, x < rectangle.width, y < rectangle.height else {
        throw InputError.invalid("Point must be inside the client window content (window-relative points, not Retina pixels). Window: \(rectangle)")
    }
    let modifiers: CGEventFlags = [.maskShift, .maskControl, .maskAlternate, .maskCommand]
    guard CGEventSource.flagsState(.combinedSessionState).intersection(modifiers).isEmpty else {
        throw InputError.invalid("A modifier is already held; refusing to interfere with user input.")
    }
    app.activate(options: [])
    Thread.sleep(forTimeInterval: 0.15)
    guard NSWorkspace.shared.frontmostApplication?.processIdentifier == pid else {
        throw InputError.invalid("Client could not become foreground; no events sent.")
    }
    let point = CGPoint(x: rectangle.minX + x, y: rectangle.minY + y)
    guard let source = CGEventSource(stateID: .hidSystemState),
          let move = CGEvent(mouseEventSource: source, mouseType: .mouseMoved, mouseCursorPosition: point, mouseButton: .left),
          let shiftDown = CGEvent(keyboardEventSource: source, virtualKey: 56, keyDown: true),
          let shiftUp = CGEvent(keyboardEventSource: source, virtualKey: 56, keyDown: false),
          let down = CGEvent(mouseEventSource: source, mouseType: .leftMouseDown, mouseCursorPosition: point, mouseButton: .left),
          let up = CGEvent(mouseEventSource: source, mouseType: .leftMouseUp, mouseCursorPosition: point, mouseButton: .left) else {
        throw InputError.invalid("Could not construct input events; no events sent.")
    }
    shiftDown.type = .flagsChanged
    shiftDown.flags = .maskShift
    shiftUp.type = .flagsChanged
    shiftUp.flags = []
    down.flags = .maskShift
    up.flags = .maskShift
    move.flags = []
    move.post(tap: .cghidEventTap)
    Thread.sleep(forTimeInterval: 0.05)
    shiftDown.post(tap: .cghidEventTap)
    // Release both inputs even if foreground changes after pressing Shift.
    defer {
        up.post(tap: .cghidEventTap)
        shiftUp.post(tap: .cghidEventTap)
    }
    Thread.sleep(forTimeInterval: 0.08)
    guard NSWorkspace.shared.frontmostApplication?.processIdentifier == pid else {
        throw InputError.invalid("Foreground changed; click cancelled and Shift released.")
    }
    down.post(tap: .cghidEventTap)
    Thread.sleep(forTimeInterval: 0.05)
    print("Posted Shift+left click: pid=\(pid), bundle=\(bundle), windowPoint=(\(x),\(y)), screenPoint=\(point). Verify the game result separately.")
}

do {
    try run()
} catch {
    FileHandle.standardError.write(Data("\(error)\n".utf8))
    exit(1)
}
