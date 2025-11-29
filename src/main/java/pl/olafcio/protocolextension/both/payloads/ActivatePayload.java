/*
 * Copyright (c) 2025 Olafcio
 * (Olafcio1 on GitHub)
 *
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 * 1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

package pl.olafcio.protocolextension.both.payloads;

import pl.olafcio.protocolextension.both.UIdentifier;

/**
 * A two-side activation payload. First, it's sent by the client, which the server should react with the same packet
 * if it supports PX. After the server responds, the client starts sending C2S packets in this session.
 * <p>
 * Depending on the implementation, the state might reset after a world change. Thus, it's recommended to resend it
 * when changing dimensions, both for the server AND the client.
 * <p>
 * <h2>Why?</h2>
 * The server might start and stop supporting PX at any time due to "proxy hosting". If you can join multiple servers
 * without reconnecting from the server list, the state might be corrupt - the server has stopped supporting PX, but
 * you are still sending C2S packets to it; then it kicks you for an invalid plugin message (custom payload).
 *
 * <br>
 * <br>
 *
 * When changing the server you're on, the server resends you the world data - and puts you in another dimension. Even
 * if it's the same name as it was, it's not the same state. That's why it's recommended to reset the state when
 * changing dimensions - that's the only reliable way to detect subserver change.
 */
public record ActivatePayload() {
    public static UIdentifier ID = new UIdentifier("protocolextension", "activate");
}
