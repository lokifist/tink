// Copyright 2018 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////

package com.google.crypto.tink.signature;

import com.google.crypto.tink.KeyManager;
import com.google.crypto.tink.PublicKeyVerify;
import com.google.crypto.tink.proto.KeyData;
import com.google.crypto.tink.proto.RsaSsaPssParams;
import com.google.crypto.tink.proto.RsaSsaPssPublicKey;
import com.google.crypto.tink.subtle.EngineFactory;
import com.google.crypto.tink.subtle.RsaSsaPssVerifyJce;
import com.google.crypto.tink.subtle.Validators;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;

/**
 * This key manager produces new instances of {@code RsaSsaPssVerifyJce}. It doesn't support key
 * generation.
 */
class RsaSsaPssVerifyKeyManager implements KeyManager<PublicKeyVerify> {
  public static final String TYPE_URL = "type.googleapis.com/google.crypto.tink.RsaSsaPssPublicKey";
  /** Current version of this key manager. Keys with greater version are not supported. */
  private static final int VERSION = 0;

  /** @param serializedKey serialized {@code RsaSsaPssPublicKey} proto */
  @Override
  public PublicKeyVerify getPrimitive(ByteString serializedKey) throws GeneralSecurityException {
    try {
      RsaSsaPssPublicKey pubKey = RsaSsaPssPublicKey.parseFrom(serializedKey);
      return getPrimitive(pubKey);
    } catch (InvalidProtocolBufferException e) {
      throw new GeneralSecurityException("expected serialized RsaSsaPssPublicKey proto", e);
    }
  }

  /** @param key {@code RsaSsaPssPublicKey} proto */
  @Override
  public PublicKeyVerify getPrimitive(MessageLite key) throws GeneralSecurityException {
    if (!(key instanceof RsaSsaPssPublicKey)) {
      throw new GeneralSecurityException("expected RsaSsaPssPublicKey proto");
    }
    RsaSsaPssPublicKey keyProto = (RsaSsaPssPublicKey) key;
    validateKey(keyProto);
    KeyFactory kf = EngineFactory.KEY_FACTORY.getInstance("RSA");
    BigInteger modulus = new BigInteger(1, keyProto.getN().toByteArray());
    BigInteger exponent = new BigInteger(1, keyProto.getE().toByteArray());
    RSAPublicKey publicKey =
        (RSAPublicKey) kf.generatePublic(new RSAPublicKeySpec(modulus, exponent));
    RsaSsaPssParams params = keyProto.getParams();
    return new RsaSsaPssVerifyJce(
        publicKey,
        SigUtil.toHashType(params.getSigHash()),
        SigUtil.toHashType(params.getMgf1Hash()),
        params.getSaltLength());
  }

  /**
   * @param serializedKeyFormat serialized {@code RsaSsaPssKeyFormat} proto
   * @return new {@code RsaSsaPssPublicKey} proto
   */
  @Override
  public MessageLite newKey(ByteString serializedKeyFormat) throws GeneralSecurityException {
    throw new GeneralSecurityException("Not implemented");
  }

  /**
   * @param keyFormat {@code RsaSsaPssKeyFormat} proto
   * @return new {@code RsaSsaPssPublicKey} proto
   */
  @Override
  public MessageLite newKey(MessageLite keyFormat) throws GeneralSecurityException {
    throw new GeneralSecurityException("Not implemented");
  }

  /**
   * @param serializedKeyFormat serialized {@code RsaSsaPssKeyFormat} proto
   * @return {@code KeyData} with a new {@code RsaSsaPssPublicKey} proto
   */
  @Override
  public KeyData newKeyData(ByteString serializedKeyFormat) throws GeneralSecurityException {
    throw new GeneralSecurityException("Not implemented");
  }

  @Override
  public boolean doesSupport(String typeUrl) {
    return TYPE_URL.equals(typeUrl);
  }

  @Override
  public String getKeyType() {
    return TYPE_URL;
  }

  @Override
  public int getVersion() {
    return VERSION;
  }

  private void validateKey(RsaSsaPssPublicKey pubKey) throws GeneralSecurityException {
    Validators.validateVersion(pubKey.getVersion(), VERSION);
    Validators.validateRsaModulusSize(new BigInteger(1, pubKey.getN().toByteArray()).bitLength());
    SigUtil.validateRsaSsaPssParams(pubKey.getParams());
  }
}
