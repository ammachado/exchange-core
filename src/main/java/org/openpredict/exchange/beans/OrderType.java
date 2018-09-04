package org.openpredict.exchange.beans;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OrderType {

    LIMIT((byte) 0),

    MARKET((byte) 1);

    @Getter
    private final byte code;
}
