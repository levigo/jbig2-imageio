/**
 * Copyright (C) 1995-2015 levigo holding gmbh.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.levigo.jbig2.image;


abstract class Filter {

  /**
   * Find a filter name by its type.
   * 
   * @param type the filter type
   * @return filter name
   */
  public static String nameByType(final FilterType type) {
    if (type == null)
      throw new IllegalArgumentException("type must not be null");
    return type.name();
  }

  /**
   * Find a filter type by its name.
   * 
   * @param name the filter name
   * @return filter type
   */
  public static FilterType typeByName(final String name) {
    if (name == null)
      throw new IllegalArgumentException("name must not be null");
    return FilterType.valueOf(name);
  }

  /**
   * Find a filter by its type.
   * 
   * @param type the filter type
   * @return the Filter
   */
  public static Filter byType(final FilterType type) {
    switch (type){
      case Bessel :
        return new Bessel();
      case Blackman :
        return new Blackman();
      case Box :
        return new Box();
      case Catrom :
        return new Catrom();
      case Cubic :
        return new Cubic();
      case Gaussian :
        return new Gaussian();
      case Hamming :
        return new Hamming();
      case Hanning :
        return new Hanning();
      case Hermite :
        return new Hermite();
      case Lanczos :
        return new Lanczos();
      case Mitchell :
        return new Mitchell();
      case Point :
        return new Point();
      case Quadratic :
        return new Quadratic();
      case Sinc :
        return new Sinc();
      case Triangle :
        return new Triangle();
    }
    throw new IllegalArgumentException("No filter for given type.");
  }

  public static final class Bessel extends Filter {
    public Bessel() {
      super(false, 3.2383, 1.0);
    }

    private double J1(final double x) {
      double p, q;

      int i;

      final double Pone[] = {
          0.581199354001606143928050809e+21, -0.6672106568924916298020941484e+20, 0.2316433580634002297931815435e+19,
          -0.3588817569910106050743641413e+17, 0.2908795263834775409737601689e+15, -0.1322983480332126453125473247e+13,
          0.3413234182301700539091292655e+10, -0.4695753530642995859767162166e+7, 0.270112271089232341485679099e+4
      }, Qone[] = {
          0.11623987080032122878585294e+22, 0.1185770712190320999837113348e+20, 0.6092061398917521746105196863e+17,
          0.2081661221307607351240184229e+15, 0.5243710262167649715406728642e+12, 0.1013863514358673989967045588e+10,
          0.1501793594998585505921097578e+7, 0.1606931573481487801970916749e+4, 0.1e+1
      };

      p = Pone[8];
      q = Qone[8];
      for (i = 7; i >= 0; i--) {
        p = p * x * x + Pone[i];
        q = q * x * x + Qone[i];
      }
      return p / q;
    }

    private double P1(final double x) {
      double p, q;

      int i;

      final double Pone[] = {
          0.352246649133679798341724373e+5, 0.62758845247161281269005675e+5, 0.313539631109159574238669888e+5,
          0.49854832060594338434500455e+4, 0.2111529182853962382105718e+3, 0.12571716929145341558495e+1
      }, Qone[] = {
          0.352246649133679798068390431e+5, 0.626943469593560511888833731e+5, 0.312404063819041039923015703e+5,
          0.4930396490181088979386097e+4, 0.2030775189134759322293574e+3, 0.1e+1
      };

      p = Pone[5];
      q = Qone[5];
      for (i = 4; i >= 0; i--) {
        p = p * (8.0 / x) * (8.0 / x) + Pone[i];
        q = q * (8.0 / x) * (8.0 / x) + Qone[i];
      }
      return p / q;
    }

    private double Q1(final double x) {
      double p, q;

      int i;

      final double Pone[] = {
          0.3511751914303552822533318e+3, 0.7210391804904475039280863e+3, 0.4259873011654442389886993e+3,
          0.831898957673850827325226e+2, 0.45681716295512267064405e+1, 0.3532840052740123642735e-1
      }, Qone[] = {
          0.74917374171809127714519505e+4, 0.154141773392650970499848051e+5, 0.91522317015169922705904727e+4,
          0.18111867005523513506724158e+4, 0.1038187585462133728776636e+3, 0.1e+1
      };

      p = Pone[5];
      q = Qone[5];
      for (i = 4; i >= 0; i--) {
        p = p * (8.0 / x) * (8.0 / x) + Pone[i];
        q = q * (8.0 / x) * (8.0 / x) + Qone[i];
      }
      return p / q;
    }

    private double BesselOrderOne(double x) {
      double p, q;

      if (x == 0.0)
        return 0.0;
      p = x;
      if (x < 0.0)
        x = -x;
      if (x < 8.0)
        return p * J1(x);
      q = Math.sqrt(2.0 / (Math.PI * x))
          * (P1(x) * (1.0 / Math.sqrt(2.0) * (Math.sin(x) - Math.cos(x))) - 8.0 / x * Q1(x)
              * (-1.0 / Math.sqrt(2.0) * (Math.sin(x) + Math.cos(x))));
      if (p < 0.0)
        q = -q;
      return q;
    }

    @Override
    public double f(final double x) {
      if (x == 0.0)
        return Math.PI / 4.0;
      return BesselOrderOne(Math.PI * x) / (2.0 * x);
    }
  }

  public static final class Blackman extends Filter {
    @Override
    public double f(final double x) {
      return 0.42 + 0.50 * Math.cos(Math.PI * x) + 0.08 * Math.cos(2.0 * Math.PI * x);
    }
  }

  public static class Box extends Filter {
    public Box() {
      super(true, .5, 1.0);
    }

    public Box(final double supp) {
      super(true, supp, 1.0);
    }

    @Override
    public double f(final double x) {
      if (x >= -0.5 && x < 0.5)
        return 1.0;
      return 0.0;
    }
  }

  public static final class Point extends Box {
    public Point() {
      super(0);
    }

    @Override
    public double fWindowed(double x) {
      // don't apply windowing as we have a radius of zero.
      return super.f(x);
    }
  }

  public static final class Catrom extends Filter {
    public Catrom() {
      super(true, 2.0, 1.0);
    }

    @Override
    public double f(double x) {
      if (x < 0)
        x = -x;
      if (x < 1.0)
        return 0.5 * (2.0 + x * x * (-5.0 + x * 3.0));
      if (x < 2.0)
        return 0.5 * (4.0 + x * (-8.0 + x * (5.0 - x)));
      return 0.0;
    }
  }

  public static final class Cubic extends Filter {
    public Cubic() {
      super(false, 2.0, 1.0);
    }

    @Override
    public double f(double x) {
      if (x < 0)
        x = -x;
      if (x < 1.0)
        return 0.5 * x * x * x - x * x + 2.0 / 3.0;
      if (x < 2.0) {
        x = 2.0 - x;
        return 1.0 / 6.0 * x * x * x;
      }
      return 0.0;
    }
  }

  public static final class Gaussian extends Filter {
    public Gaussian() {
      super(false, 1.25, 1.0);
    }

    @Override
    public double f(final double x) {
      return Math.exp(-2.0 * x * x) * Math.sqrt(2.0 / Math.PI);
    }
  }

  public static final class Hamming extends Filter {
    @Override
    public double f(final double x) {
      return 0.54 + 0.46 * Math.cos(Math.PI * x);
    }
  }

  public static final class Hanning extends Filter {
    @Override
    public double f(final double x) {
      return 0.5 + 0.5 * Math.cos(Math.PI * x);
    }
  }

  public static final class Hermite extends Filter {
    @Override
    public double f(double x) {
      if (x < 0) {
        x = -x;
      }

      if (x < 1.0) {
        return (2.0 * x - 3.0) * x * x + 1.0;
      }
      return 0.0;
    }
  }

  public static final class Lanczos extends Filter {
    public Lanczos() {
      super(true, 3.0, 1.0);
    }

    @Override
    public double f(double x) {
      if (x < 0)
        x = -x;
      if (x < 3.0)
        return (float) (sinc(x) * sinc(x / 3.0));
      return 0.0;
    }

    private double sinc(double value) {
      if (value != 0.0f) {
        value = value * Math.PI;
        return Math.sin(value) / value;
      } else {
        return 1.0;
      }
    }

  }

  public static final class Mitchell extends Filter {
    public Mitchell() {
      super(false, 2.0, 1.0);
    }

    @Override
    public double f(double x) {
      double b, c;

      b = 1.0 / 3.0;
      c = 1.0 / 3.0;
      if (x < 0)
        x = -x;
      if (x < 1.0) {
        x = (12.0 - 9.0 * b - 6.0 * c) * (x * x * x) + (-18.0 + 12.0 * b + 6.0 * c) * x * x + (6.0 - 2.0 * b);
        return x / 6.0;
      }
      if (x < 2.0) {
        x = (-1.0 * b - 6.0 * c) * (x * x * x) + (6.0 * b + 30.0 * c) * x * x + (-12.0 * b - 48.0 * c) * x
            + (8.0 * b + 24.0 * c);
        return x / 6.0;
      }
      return 0.0;
    }
  }

  public static final class Quadratic extends Filter {
    public Quadratic() {
      super(false, 1.5, 1.0);
    }

    @Override
    public double f(double x) {
      if (x < 0)
        x = -x;
      if (x < 0.5)
        return 0.75 - x * x;
      if (x < 1.5) {
        x -= 1.5;
        return 0.5 * x * x;
      }
      return 0.0;
    }
  }

  public static final class Sinc extends Filter {
    public Sinc() {
      super(true, 4.0, 1.0);
    }

    @Override
    public double f(double x) {
      x *= Math.PI;
      if (x != 0.0)
        return Math.sin(x) / x;
      return 1.0;
    }
  }

  public static final class Triangle extends Filter {
    @Override
    public double f(double x) {
      if (x < 0.0)
        x = -x;
      if (x < 1.0)
        return 1.0 - x;
      return 0.0;
    }
  }

  /**
   * is this filter cardinal? ie, does func(x) = (x==0) for integer x?
   */
  final boolean cardinal;

  /** radius of nonzero portion */
  double support;

  /** blur factor (1=normal) */
  double blur;

  protected Filter() {
    this(true, 1.0, 1.0);
  }

  protected Filter(final boolean cardinal, final double support, final double blur) {
    this.cardinal = cardinal;
    this.support = support;
    this.blur = blur;
  }

  public double fWindowed(double x) {
    return x < -support || x > support ? 0 : f(x);
  }

  public abstract double f(double x);

  /**
   * Return the filter name.
   * 
   * @return the filter's name
   */
  public String getName() {
    return getClass().getSimpleName();
  }

  /**
   * @return the support
   */
  public double getSupport() {
    return support;
  }

  /**
   * @param support the support to set
   */
  public void setSupport(final double support) {
    this.support = support;
  }

  /**
   * @return the blur
   */
  public double getBlur() {
    return blur;
  }

  /**
   * @param blur the blur to set
   */
  public void setBlur(final double blur) {
    this.blur = blur;
  }
}
