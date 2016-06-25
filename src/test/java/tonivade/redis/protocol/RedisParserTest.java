/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package tonivade.redis.protocol;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static tonivade.redis.protocol.SafeString.safeString;

import org.junit.Test;
import org.mockito.Mockito;

import tonivade.redis.protocol.RedisToken.UnknownRedisToken;

public class RedisParserTest {

    private RedisSource source = Mockito.mock(RedisSource.class);

    private RedisParser parser = new RedisParser(100000, source);

    private RedisToken intToken = RedisToken.integer(1);
    private RedisToken abcString = RedisToken.string("abc");
    private RedisToken pongString = RedisToken.status("pong");
    private RedisToken errorString = RedisToken.error("ERR");
    private RedisToken arrayToken = RedisToken.array(intToken, abcString);
    private RedisToken unknownString = new UnknownRedisToken(safeString("what?"));

    @Test
    public void testBulkString() {
        when(source.readLine()).thenReturn(safeString("$3"));
        when(source.readString(3)).thenReturn(safeString("abc"));

        RedisToken token = parser.parse();

        assertThat(token, equalTo(abcString));
    }

    @Test
    public void testSimpleString() {
        when(source.readLine()).thenReturn(safeString("+pong"));

        RedisToken token = parser.parse();

        assertThat(token, equalTo(pongString));
    }

    @Test
    public void testInteger() {
        when(source.readLine()).thenReturn(safeString(":1"));

        RedisToken token = parser.parse();

        assertThat(token, equalTo(intToken));
    }

    @Test
    public void testErrorString() {
        when(source.readLine()).thenReturn(safeString("-ERR"));

        RedisToken token = parser.parse();

        assertThat(token, equalTo(errorString));
    }

    @Test
    public void testUnknownString() {
        when(source.readLine()).thenReturn(safeString("what?"));

        RedisToken token = parser.parse();

        assertThat(token, equalTo(unknownString));
    }

    @Test
    public void testArray() {
        when(source.readLine()).thenReturn(safeString("*2"), safeString(":1"), safeString("$3"));
        when(source.readString(3)).thenReturn(safeString("abc"));

        RedisToken token = parser.parse();

        assertThat(token, equalTo(arrayToken));
    }

}
