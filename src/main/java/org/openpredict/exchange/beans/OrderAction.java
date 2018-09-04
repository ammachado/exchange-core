package org.openpredict.exchange.beans;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OrderAction {

    ASK((byte) 0),

    BID((byte) 1);

    @Getter
    private final byte code;

    public OrderAction opposite() {
        return this == ASK ? BID : ASK;
    }
}
