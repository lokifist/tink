import HybridEncryptWrapper from 'goog:tink.hybrid.HybridEncryptWrapper'; // from //third_party/tink/javascript/hybrid:hybrid_wrappers
import * as Registry from '../internal/registry';

export function register() {
  Registry.registerPrimitiveWrapper(new HybridEncryptWrapper());
}
