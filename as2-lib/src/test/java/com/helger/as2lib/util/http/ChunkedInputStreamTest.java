/**
 * The FreeBSD Copyright
 * Copyright 1994-2008 The FreeBSD Project. All rights reserved.
 * Copyright (C) 2013-2019 Philip Helger philip[at]helger[dot]com
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE FREEBSD PROJECT ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE FREEBSD PROJECT OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation
 * are those of the authors and should not be interpreted as representing
 * official policies, either expressed or implied, of the FreeBSD Project.
 */
package com.helger.as2lib.util.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.EOFException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.helger.commons.collection.ArrayHelper;
import com.helger.commons.io.stream.NonBlockingByteArrayInputStream;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Test class for class {@link ChunkedInputStream}.
 *
 * @author Ziv Harpaz
 */
public final class ChunkedInputStreamTest
{
  @Test (expected = EOFException.class)
  @SuppressFBWarnings ("RR_NOT_CHECKED")
  public void readBufferFromEmpty () throws Exception
  {
    final InputStream empty = new NonBlockingByteArrayInputStream ("".getBytes ());
    final byte [] buf = new byte [17];
    try (final ChunkedInputStream cIS = new ChunkedInputStream (empty))
    {
      cIS.read (buf, 0, buf.length);
      fail ("An EOFException should have been thrown");
    }
  }

  @Test
  public void readPastEOS () throws Exception
  {
    try (final InputStream empty = new NonBlockingByteArrayInputStream ("3\n123\r\n0\r\n".getBytes ());
        final ChunkedInputStream cIS = new ChunkedInputStream (empty))
    {
      final byte [] buf = new byte [17];
      int ret = cIS.read (buf, 0, buf.length);
      assertEquals ("read correct num of bytes", ret, 3);
      assertEquals ("read the chunk", new String (buf, 0, ret), "123");
      ret = cIS.read (buf, 0, buf.length);
      assertEquals ("read past EOS", ret, -1);
    }
  }

  @Test (expected = EOFException.class)
  public void readByteFromEmpty () throws Exception
  {
    try (final InputStream empty = new NonBlockingByteArrayInputStream (ArrayHelper.EMPTY_BYTE_ARRAY);
        final ChunkedInputStream cIS = new ChunkedInputStream (empty))
    {
      cIS.read ();
      fail ("An EOFException should have been thrown");
    }
  }

  @Test
  public void readOneChunkBuffer () throws Exception
  {
    try (final InputStream empty = new NonBlockingByteArrayInputStream ("3\n123".getBytes ());
        final ChunkedInputStream cIS = new ChunkedInputStream (empty))
    {
      final byte [] buf = new byte [3];
      final int ret = cIS.read (buf, 0, buf.length);
      assertEquals ("Read one chunk: 3 chars read", 3, ret);
      assertEquals ("Read one Chunk: corect data returned", "123", new String (buf, StandardCharsets.ISO_8859_1));
    }
  }

  @Test
  public void readOneChunkBytes () throws Exception
  {
    try (final InputStream empty = new NonBlockingByteArrayInputStream ("3\n123".getBytes ());
        final ChunkedInputStream cIS = new ChunkedInputStream (empty))
    {
      int ret = cIS.read ();
      assertEquals ("Read first char", '1', ret);
      ret = cIS.read ();
      assertEquals ("Read second char", '2', ret);
      ret = cIS.read ();
      assertEquals ("Read third char", '3', ret);
    }
  }

  @Test
  public void readTwoChunkBuffer () throws Exception
  {
    try (final InputStream empty = new NonBlockingByteArrayInputStream ("2\r\n12\r\n1\na\r\n0\r\n".getBytes ());
        final ChunkedInputStream cIS = new ChunkedInputStream (empty))
    {
      final byte [] buf = new byte [3];
      final int ret = cIS.read (buf, 0, buf.length);
      assertEquals ("Read two chunk: 3 chars read", 3, ret);
      assertEquals ("Read one Chunk: corect data returned", "12a", new String (buf));
    }
  }

  @Test
  public void readTwoChunkBufferMultipleReads () throws Exception
  {
    try (final InputStream empty = new NonBlockingByteArrayInputStream ("2\r\n12\r\n1\na\r\n0\r\n".getBytes ());
        final ChunkedInputStream cIS = new ChunkedInputStream (empty))
    {
      final byte [] buf = new byte [3];
      int ret = cIS.read (buf, 0, 1);
      assertEquals ("Read two chunk-1: 1 chars read", 1, ret);
      assertEquals ("Read one Chunk-1: correct data returned", '1', buf[0]);
      ret = cIS.read (buf, 0, 1);
      assertEquals ("Read two chunk-1: 1 chars read", 1, ret);
      assertEquals ("Read one Chunk-1: correct data returned", '2', buf[0]);
      ret = cIS.read (buf, 0, 1);
      assertEquals ("Read two chunk-1: 1 chars read", 1, ret);
      assertEquals ("Read one Chunk-1: correct data returned", 'a', buf[0]);
    }
  }

  @Test
  public void readTwoChunkByteMultipleReads () throws Exception
  {
    try (final InputStream empty = new NonBlockingByteArrayInputStream ("2\r\n12\r\n1\na\r\n0\r\n".getBytes ());
        final ChunkedInputStream cIS = new ChunkedInputStream (empty))
    {
      int ret = cIS.read ();
      assertEquals ("Read one Chunk-1: correct data returned", '1', ret);
      ret = cIS.read ();
      assertEquals ("Read one Chunk-1: correct data returned", '2', ret);
      ret = cIS.read ();
      assertEquals ("Read one Chunk-1: correct data returned", 'a', ret);
    }
  }

}
