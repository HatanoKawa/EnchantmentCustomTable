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
    var args = Array(CommandLine.arguments.dropFirst())
    let mode = args.first ?? ""
    let plain = ["--plain", "--right", "--move"].contains(mode)
    if plain { args.removeFirst() }
    if args == ["--check"] {
        print("accessibilityTrusted=\(AXIsProcessTrusted())")
        let flags = CGEventSource.flagsState(.combinedSessionState)
        let names: [(String, CGEventFlags)] = [("Shift", .maskShift), ("Control", .maskControl),
                                               ("Option", .maskAlternate), ("Command", .maskCommand)]
        print("heldModifiers=\(names.filter { flags.contains($0.1) }.map { $0.0 }.joined(separator: ","))")
        return
    }
    let releaseOnly = args.count == 2 && args[0] == "--release-shift"
    guard (args.count == 3 || releaseOnly), let pid = Int32(args[releaseOnly ? 1 : 0]),
          let x = Double(releaseOnly ? "0" : args[1]),
          let y = Double(releaseOnly ? "24" : args[2]), x.isFinite, y.isFinite else {
        throw InputError.invalid("Usage: shift-click --check | --release-shift <client-pid> | [--plain|--right|--move] <client-pid> <window-x-points> <window-y-points>")
    }
    guard AXIsProcessTrusted() else {
        throw InputError.invalid("Accessibility permission is unavailable; no events sent. No permission prompt or settings change was requested.")
    }
    // Explicit test matrix only; do not accept arbitrary applications by prefix.
    var allowed = ["com.river-quinn.ect.gui-dev.fabric.mc1211", "com.river-quinn.ect.gui-dev.mc1211"]
    for version in ["1-21-2", "1-21-4", "1-21-5", "1-21-6", "1-21-9", "1-21-11", "26-1", "26-2", "26-3"] {
        for loader in ["fabric", "neoforge"] {
            allowed.append("com.river-quinn.ect.gui-dev.\(loader).mc\(version)")
        }
    }
    for version in ["1-18-2", "1-19-2", "1-20-1"] {
        for loader in ["fabric", "forge"] {
            allowed.append("com.river-quinn.ect.gui-dev.\(loader).mc\(version)")
        }
    }
    guard let app = NSRunningApplication(processIdentifier: pid),
          let bundle = app.bundleIdentifier, allowed.contains(bundle) else {
        throw InputError.invalid("Target must be an explicitly allowed ECT GUI matrix client.")
    }
    if releaseOnly {
        // Recovery only for a Shift left held by this tool; never use during manual input.
        guard NSWorkspace.shared.frontmostApplication?.processIdentifier == pid,
              CGEventSource.flagsState(.combinedSessionState)
                .intersection([.maskControl, .maskAlternate, .maskCommand]).isEmpty,
              let source = CGEventSource(stateID: .hidSystemState),
              let release = CGEvent(keyboardEventSource: source, virtualKey: 56, keyDown: false) else {
            throw InputError.invalid("Recovery requires the test client to be foreground.")
        }
        release.type = .flagsChanged
        release.flags = []
        release.post(tap: .cghidEventTap)
        Thread.sleep(forTimeInterval: 0.1)
        print("Posted left Shift release; heldShift=\(CGEventSource.flagsState(.combinedSessionState).contains(.maskShift))")
        return
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
          let down = CGEvent(mouseEventSource: source, mouseType: mode == "--right" ? .rightMouseDown : .leftMouseDown, mouseCursorPosition: point, mouseButton: mode == "--right" ? .right : .left),
          let up = CGEvent(mouseEventSource: source, mouseType: mode == "--right" ? .rightMouseUp : .leftMouseUp, mouseCursorPosition: point, mouseButton: mode == "--right" ? .right : .left) else {
        throw InputError.invalid("Could not construct input events; no events sent.")
    }
    shiftDown.type = .flagsChanged
    shiftDown.flags = .maskShift
    shiftUp.type = .flagsChanged
    shiftUp.flags = []
    down.flags = plain ? [] : .maskShift
    up.flags = plain ? [] : .maskShift
    move.flags = []
    move.post(tap: .cghidEventTap)
    Thread.sleep(forTimeInterval: 0.05)
    if mode == "--move" {
        print("Posted mouse move: pid=\(pid), windowPoint=(\(x),\(y)). Verify the game hover separately.")
        return
    }
    if !plain { shiftDown.post(tap: .cghidEventTap) }
    // Release both inputs even if foreground changes after pressing Shift.
    defer {
        up.post(tap: .cghidEventTap)
        Thread.sleep(forTimeInterval: 0.05)
        if !plain { shiftUp.post(tap: .cghidEventTap) }
        // Keep the event source alive while the release reaches the event queue.
        Thread.sleep(forTimeInterval: 0.1)
    }
    Thread.sleep(forTimeInterval: 0.08)
    guard NSWorkspace.shared.frontmostApplication?.processIdentifier == pid else {
        throw InputError.invalid("Foreground changed; click cancelled and posted inputs released.")
    }
    down.post(tap: .cghidEventTap)
    Thread.sleep(forTimeInterval: 0.05)
    print("Posted \(plain ? mode : "Shift+left") click: pid=\(pid), bundle=\(bundle), windowPoint=(\(x),\(y)), screenPoint=\(point). Verify the game result separately.")
}

do {
    try run()
} catch {
    FileHandle.standardError.write(Data("\(error)\n".utf8))
    exit(1)
}
