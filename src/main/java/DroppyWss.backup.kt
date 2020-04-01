//    val client = HttpClient(OkHttp) {
//        install(WebSockets)
//        install(HttpCookies) {
//            storage = AcceptAllCookiesStorage()
//        }
//    }
//
//    runBlocking {
//        val init = client.get<HttpResponse>("https://droppy.ironrobin.net")
//
//        val cake = init.headers["Set-Cookie"]?.split(";")?.getOrNull(0)
//        println("init: ${cake}")
//
//        val token = client.get<String>("https://droppy.ironrobin.net/!/token") {
//            header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:74.0) Gecko/20100101 Firefox/74.0")
//            header("Accept", "*/*")
//            header("Accept-Language", "en,en-US;q=0.7,de;q=0.3")
//            header("Accept-Encoding", "gzip, deflate, br")
//            header("x-app", "droppy")
//            header("DNT", "1")
//            header("Connection", "keep-alive")
//            header("Cookie", cake)
//            header("TE", "Trailers")
//        }
//        println("token: $token")
//
//        client.wss(
//                method = HttpMethod.Get,
//                host = "165.227.31.192",
//                port = 443,
//                path = "/!/socket",
//                request = {
//                    header("Cookie", cake)
//                    header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:74.0) Gecko/20100101 Firefox/74.0")
//                    header("Accept", "*/*")
//                    header("Accept-Language", "en,en-US;q=0.7,de;q=0.3")
//                    // header("Accept-Encoding", "gzip, deflate, br")
//                    header("Sec-WebSocket-Version", "13")
//                    header("Origin", "https://droppy.ironrobin.net")
//                    header("Sec-WebSocket-Extensions", "permessage-deflate")
//                    header("Sec-WebSocket-Key", "BOKlTvd3QN4RnMdyomxpOg==")
//                    header("DNT", "1")
//                    header("Connection", "keep-alive, Upgrade")
//                    header("Pragma", "no-cache")
//                    header("Cache-Control", "no-cache")
//                    header("Origin","https://droppy.ironrobin.net")
//                }
//        ) {
//            // Send text frame.
//            // send(Frame.Ping())
//            send(Frame.Text("{\"vId\":0,\"type\":\"REQUEST_UPDATE\",\"data\":\"/Images/PureOS\",\"token\":\"$token\"}"))
//
//            // Receive frame.
//            val frame = incoming.receive()
//            when (frame) {
//                is Frame.Text -> println(frame.readText())
//            }
//        }
//    }