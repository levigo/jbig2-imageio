# Release notes

## Version 1.6.3 (2014-08.04
- Issue #1: Discard the request of copying the row above the first row.

## Version 1.6.2-RC1 (2014-01-22)
- Googlecode Issue 17: Changed computation of result dimension from ceiling to rounding. 

##Version 1.6.1 (2013-04-18)
- Googlecode Issue 11: Added support for `GRREFERENCE` Release notes

## Version 1.6.2-RC1 (2014-01-22)
- Googlecode Issue 17: Changed computation of result dimension from ceiling to rounding. 

## Version 1.6.1 (2013-04-18)
- Googlecode Issue 11: Added support for `GRREFERENCEDX` in generic refinement region coding. 

## Version 1.6.0 (2013-03-25):
- Googlecode Issue 10: Usability of `CacheFactory` and `LoggerFactory` improved.

## Version 1.5.2 (2012-10-09):
- Googlecode Issue  9: Transfer of bitmap's data into target raster went wrong if bitmap's line ends with a padded byte. 

## Version 1.5.1 (2012-10-02):
- Googlecode Issue  8: The default read parameters changed. There will be no source region, no source render size (no scaling) 
  and subsampling factors of 1 (no subsampling). `Bitmaps.java` can handle this correctly.

## Version 1.5.0 (2012-09-20):
- Moved Exception-classes to dedicated package `com.levigo.jbig2.err`.
- Introduced a new utility class `com.levigo.jbig2.image.Bitmaps`. This class provides a bunch of new features operating 
  on a `Bitmap` instance. For example, extracting a region of interest, scaling with high-quality filters and subsampling
  in either or both horizontal and vertical direction.

## Version 1.4.1 (2012-04-20):
- Fixed race condition when parsing documents with multiple pages.

## Version 1.4 (2012-04-10):
- Googlecode Issue  6 : The returned bitmap was too small in case of only one region. Solution is to check if we have only one
  region that forms the complete page. Only if width and height of region equals width and height of page use region's
  bitmap as the page's bitmap. 
- Googlecode Issue  5: A raster which was too small was created. AWT has thrown a `RasterFormatException`.  
- Googlecode Issue  4: `IndexOutOfBoundsException` indicates the end of the stream in `JBIG2Document#reachedEndOfStream()`
- Googlecode Issue  3: Reader recognizes if a file header is present or not.

## Version 1.3 (2011-10-28):
- Untracked Googlecode Issue : Fixed inverted color model for grayscale images.
- Untracked Googlecode Issue : Fixed `IndexArrayOutOfBoundException` in handling requests with region of interests. The region of
  interest is clipped at image boundary.

## Version 1.2 (2011-10-06):
- Googlecode Issue  1: The default read parameters will return a default image size of 1x1 without claiming the missing input.
- Untracked Googlecode Issue : A black pixel was represented by 1 and a white pixel by 0. For work with image masks the
  convention says, a black pixel is the minimum and the white pixel is maximum. This corresponds to an additive
  colorspace. We turned the representation of white and black pixels for conformity.

## Version 1.1 (2010-12-13):
- raster creation optimized
- potential NPE in cache 

## Version 1.0 (2010-07-29):
- open-source'd  DX in generic refinement region coding. 
