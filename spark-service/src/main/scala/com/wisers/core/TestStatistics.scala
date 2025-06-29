package com.wisers.core

import com.wisers.UnitSpec

/**
 * 编写一些统计和实战信息
 * Top10热门品类
 * Top10热门品类中每个品类的Top10活跃Session统计
 * 页面单跳转换率统计
 */
class TestStatistics extends UnitSpec{

  /**
   * 鞋			点击数 下单数  支付数
   * 衣服	  点击数 下单数  支付数
   * 电脑		点击数 下单数  支付数
   * 综合排名 = 点击数*20%+下单数*30%+支付数*50%
   * 先按照点击数排名，靠前的就排名高；如果点击数相同，再比较下单数；下单数再相同，就比较支付数。
   */
  test("test Top10热门品类") {

  }

  /**
   * 在需求一的基础上，增加每个品类用户session的点击统计
   */
  test("test Top10热门品类中每个品类的Top10活跃Session统计") {

  }

  /**
   * 页面单跳转换率统计
   * 计算页面单跳转化率，什么是页面单跳转换率，比如一个用户在一次 Session 过程中访问的页面路径 3,5,7,9,10,21，
   * 那么页面 3 跳到页面 5 叫一次单跳，7-9 也叫一次单跳，那么单跳转化率就是要统计页面点击的概率。
   * 比如：计算 3-5 的单跳转化率，先获取符合条件的 Session 对于页面 3 的访问次数（PV）为 A，
   * 然后获取符合条件的 Session 中访问了页面 3 又紧接着访问了页面 5 的次数为 B，那么 B/A 就是 3-5 的页面单跳转化率。
   * 首页-->产品列表-->页面详情-->订单页面-->支付页面
   *
   * 产品经理和运营总监，可以根据这个指标，去尝试分析，整个网站，产品，各个页面的表现怎么样，是不是需要去优化产品的布局；
   *    吸引用户最终可以进入最后的支付页面。
   * 数据分析师，可以此数据做更深一步的计算和分析。
   * 企业管理层，可以看到整个公司的网站，各个页面的之间的跳转的表现如何，可以适当调整公司的经营战略或策略。
   */
  test("test 页面单跳转换率统计") {

  }

}
