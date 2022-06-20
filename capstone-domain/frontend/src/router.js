
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import OrderManager from "./components/listers/OrderCards"

import OrderStatus from "./components/OrderStatus"
import PayManager from "./components/listers/PayCards"

import DeliveryManager from "./components/listers/DeliveryCards"

import ProductManager from "./components/listers/ProductCards"

export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/orders',
                name: 'OrderManager',
                component: OrderManager
            },

            {
                path: '/orderStatuses',
                name: 'OrderStatus',
                component: OrderStatus
            },
            {
                path: '/pays',
                name: 'PayManager',
                component: PayManager
            },

            {
                path: '/deliveries',
                name: 'DeliveryManager',
                component: DeliveryManager
            },

            {
                path: '/products',
                name: 'ProductManager',
                component: ProductManager
            },



    ]
})
