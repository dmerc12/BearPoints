package com.bearpoints.api.exception;

import java.io.IOException;

@FunctionalInterface
public interface RunnableThrowing {
    void run() throws IOException;
}
