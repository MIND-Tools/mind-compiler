/**
 * Copyright (C) 2010 STMicroelectronics
 *
 * This file is part of "Mind Compiler" is free software: you can redistribute 
 * it and/or modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact: mind@ow2.org
 *
 * Authors: Matthieu Leclercq
 * Contributors: 
 */

package org.ow2.mind;

import static org.testng.Assert.fail;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.StringTemplateGroupLoader;
import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.ow2.mind.adl.DefinitionHeader;
import org.ow2.mind.st.STLoaderFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class YATLTest extends AbstractFunctionalTest {

  StringTemplateGroupLoader templateGroupLoaderItf;
  DefinitionHeader          componentHeaderGenerator;
  StringTemplateGroup       templateGroup;

  @Override
  @BeforeMethod(alwaysRun = true)
  protected void setup() throws Exception {
    super.setup();
    templateGroupLoaderItf = STLoaderFactory.newSTLoader();

    templateGroup = templateGroupLoaderItf.loadGroup(
        "st.definitions.header.Component", AngleBracketTemplateLexer.class,
        null);

    componentHeaderGenerator = new DefinitionHeader();
  }

  @Test(groups = {"functional"})
  public void test1() throws Exception {
    initSourcePath("common", "functional");
    final Definition d = runner.load("helloworld.Client");

    final String generate = componentHeaderGenerator.generate(d);
    System.out.println(generate);
  }

  @Test(groups = {"functional"})
  public void test2() throws Exception {
    initSourcePath("common", "functional");
    final Definition d = runner.load("helloworld.Helloworld");
    final String generate = componentHeaderGenerator.generate(d);
    System.out.println(generate);
  }

  @Test(groups = {"perf"})
  public void testPerf1() throws Exception {
    initSourcePath("common", "functional");
    final Definition d = runner.load("helloworld.Client");

    final double yatlTime = test(100, 10000, new Runnable() {
      public void run() {
        try {
          componentHeaderGenerator.generate(d);
        } catch (final ADLException e) {
          fail("generator raise an exception", e);
        }
      }
    });

    final double stTime = test(100, 1000, new Runnable() {
      public void run() {
        final StringTemplate st = templateGroup
            .getInstanceOf("ComponentDefinitionHeader");
        st.setAttribute("definition", d);
        st.toString();
      }
    });

    System.out.println("ST generator time  : " + stTime + "ms");
    System.out.println("YATL generator time: " + yatlTime + "ms");
    System.out.println("              ratio: " + stTime / yatlTime);
  }

  @Test(groups = {"perf"})
  public void testPerf2() throws Exception {
    initSourcePath("common", "functional");
    final Definition d = runner.load("helloworld.Client");

    // estimate output size
    final int size = (int) (componentHeaderGenerator.generate(d).length() * 1.1);

    final double yatlTime = test(100, 10000, new Runnable() {
      public void run() {
        try {
          componentHeaderGenerator.generate(d);
        } catch (final ADLException e) {
          fail("generator raise an exception", e);
        }
      }
    });

    final double yatlTime2 = test(100, 10000, new Runnable() {
      public void run() {
        try {
          componentHeaderGenerator.generate(d, new StringBuilder(size));
        } catch (final ADLException e) {
          fail("generator raise an exception", e);
        }
      }
    });

    System.out.println("base YATL generator time : " + yatlTime + "ms");
    System.out.println("sized YATL generator time: " + yatlTime2 + "ms");
    System.out.println("                    ratio: " + yatlTime / yatlTime2);
  }

  @Test(groups = {"stat_perf"})
  public void testStatPerf1() throws Exception {
    initSourcePath("common", "functional");
    final Definition d = runner.load("helloworld.Client");

    final double[] yatlSamples = statTest(100, 10000, 100, new Runnable() {
      public void run() {
        try {
          componentHeaderGenerator.generate(d);
        } catch (final ADLException e) {
          fail("generator raise an exception", e);
        }
      }
    });
    final double[][] yatlDist = dist(yatlSamples, 10);
    final double yatlMean = mean(yatlSamples);

    final double[] stSamples = statTest(100, 1000, 100, new Runnable() {
      public void run() {
        final StringTemplate st = templateGroup
            .getInstanceOf("ComponentDefinitionHeader");
        st.setAttribute("definition", d);
        st.toString();
      }
    });
    final double[][] stDist = dist(stSamples, 10);
    final double stMean = mean(stSamples);

    // estimate output size
    final int size = (int) (componentHeaderGenerator.generate(d).length() * 1.1);
    final double[] yatlSamples2 = statTest(100, 10000, 100, new Runnable() {
      public void run() {
        try {
          componentHeaderGenerator.generate(d, new StringBuilder(size));
        } catch (final ADLException e) {
          fail("generator raise an exception", e);
        }
      }
    });
    final double[][] yatlDist2 = dist(yatlSamples2, 10);
    final double yatlMean2 = mean(yatlSamples2);

    System.out.printf("# ST generator time        : %4fms\n", stMean);
    System.out.printf("# base YATL generator time : %4fms ratio: %3f\n",
        yatlMean, stMean / yatlMean);
    System.out.printf("# sized YATL generator time: %4fms ratio: %3f\n",
        yatlMean2, stMean / yatlMean2);
    System.out.printf("#\n");
    System.out.printf("# Distribution\n");
    System.out.printf("# i\tst\tyatl\tyatl2\n");
    for (int i = 0; i < yatlDist.length; i++) {
      System.out.printf(" %d\t%2f\t%2d\t%2f\t%2d\t%2f\t%2d\n", i, stDist[i][0],
          (int) stDist[i][1], yatlDist[i][0], (int) yatlDist[i][1],
          yatlDist2[i][0], (int) yatlDist2[i][1]);
    }
  }

  protected double test(final int warmup, final int nbIter, final Runnable run) {
    for (int i = 0; i < warmup; i++) {
      run.run();
    }
    long time = 0;
    for (int i = 0; i < nbIter / 100; i++) {
      System.gc();
      final long start = System.currentTimeMillis();
      for (int j = 0; j < 100; j++) {
        run.run();
      }
      time += System.currentTimeMillis() - start;
    }
    return ((double) time) / nbIter;
  }

  protected double[] statTest(final int warmup, final int nbIter,
      final int nbSample, final Runnable run) {
    final double[] result = new double[nbSample];
    for (int i = 0; i < warmup; i++) {
      run.run();
    }
    for (int i = 0; i < nbSample; i++) {
      result[i] = test(0, nbIter, run);
    }
    return result;
  }

  protected double[][] dist(final double[] samples, final int nbHisto) {
    final double[][] result = new double[nbHisto][];
    double min = Double.MAX_VALUE;
    double max = 0;
    for (final double sample : samples) {
      if (sample < min) min = sample;
      if (sample > max) max = sample;
    }
    final double step = (max - min) / nbHisto;
    for (int i = 0; i < nbHisto; i++) {
      result[i] = new double[2];
      result[i][0] = min + i * step;
      result[i][1] = 0;
    }
    for (final double sample : samples) {
      int index = (int) ((sample - min) / step);
      if (sample == max) index = nbHisto - 1;
      assert index >= 0 && index < nbHisto;
      result[index][1]++;
    }
    return result;
  }

  protected double mean(final double[] samples) {
    double sum = 0;
    for (final double sample : samples) {
      sum += sample;
    }
    return sum / samples.length;
  }
}
