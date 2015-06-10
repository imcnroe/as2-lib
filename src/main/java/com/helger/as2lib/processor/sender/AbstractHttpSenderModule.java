/**
 * The FreeBSD Copyright
 * Copyright 1994-2008 The FreeBSD Project. All rights reserved.
 * Copyright (C) 2013-2015 Philip Helger philip[at]helger[dot]com
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
package com.helger.as2lib.processor.sender;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.security.GeneralSecurityException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import com.helger.as2lib.exception.OpenAS2Exception;
import com.helger.as2lib.exception.WrappedOpenAS2Exception;
import com.helger.as2lib.util.http.DoNothingTrustManager;
import com.helger.as2lib.util.http.HostnameVerifierAlwaysTrue;
import com.helger.commons.annotations.Nonempty;
import com.helger.commons.random.VerySecureRandom;

public abstract class AbstractHttpSenderModule extends AbstractSenderModule
{
  public static final String ATTR_READ_TIMEOUT = "readtimeout";
  public static final String ATTR_CONNECT_TIMEOUT = "connecttimeout";

  @Nonnull
  public HttpURLConnection getConnection (@Nonnull @Nonempty final String sUrl,
                                          final boolean bOutput,
                                          final boolean bInput,
                                          final boolean bUseCaches,
                                          @Nonnull @Nonempty final String sRequestMethod,
                                          @Nullable final Proxy aProxy) throws OpenAS2Exception
  {
    try
    {
      final URL aUrlObj = new URL (sUrl);
      final HttpURLConnection aConn = (HttpURLConnection) (aProxy == null ? aUrlObj.openConnection ()
                                                                         : aUrlObj.openConnection (aProxy));
      aConn.setDoOutput (bOutput);
      aConn.setDoInput (bInput);
      aConn.setUseCaches (bUseCaches);
      aConn.setRequestMethod (sRequestMethod);
      aConn.setConnectTimeout (getAttributeAsInt (ATTR_CONNECT_TIMEOUT, 60000));
      aConn.setReadTimeout (getAttributeAsInt (ATTR_READ_TIMEOUT, 60000));

      if (aConn instanceof HttpsURLConnection)
      {
        // SSL handling
        final HttpsURLConnection aConns = (HttpsURLConnection) aConn;

        // Trust all server certificates
        final SSLContext aSSLCtx = SSLContext.getInstance ("TLS");
        aSSLCtx.init (null, new TrustManager [] { new DoNothingTrustManager () }, VerySecureRandom.getInstance ());
        aConns.setSSLSocketFactory (aSSLCtx.getSocketFactory ());

        // Trust all host names
        aConns.setHostnameVerifier (new HostnameVerifierAlwaysTrue ());
      }

      return aConn;
    }
    catch (final IOException ex)
    {
      throw WrappedOpenAS2Exception.wrap (ex);
    }
    catch (final GeneralSecurityException ex)
    {
      throw WrappedOpenAS2Exception.wrap (ex);
    }
  }
}
