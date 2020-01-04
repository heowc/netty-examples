package com.example;

import org.junit.jupiter.api.Test;

class EchoTest {

    @Test
    void client() throws Exception {
        EchoClient client = new EchoClient("127.0.0.1", 8080);
        client.start();
    }
}
