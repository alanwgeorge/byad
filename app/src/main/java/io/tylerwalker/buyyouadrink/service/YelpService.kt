package io.tylerwalker.buyyouadrink.service

import com.yelp.fusion.client.connection.YelpFusionApiFactory


class YelpService {
    private val apiKey = "5QIID6d3tfk3LYmaXKPIJNvKPAqf6rbWs0i_IHAc1nWPC1OTXWzUnUPrLcaSR2jN9GkbNs-ATi_r_0oScbBob8lUBNWKX11LvELwP8i6-yQuHwbwWJNErnjYi7nJWHYx"
    val api = YelpFusionApiFactory().createAPI(apiKey)
}