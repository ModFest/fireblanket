/**
 * Vanilla uses GZip compression for its various files on disk, which is slow and does not have very
 * good ratios. We're getting bottlenecked on save performance with it, so these mixins patch the
 * game to use the faster and better Zstandard algorithm.
 */
package net.modfest.fireblanket.mixin.zstd;