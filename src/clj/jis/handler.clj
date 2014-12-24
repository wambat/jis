(ns jis.handler
  (:require 
            [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.anti-forgery :as ring-anti-forgery]
            [ring.middleware.reload :as reload]
            [environ.core :refer [env]]
            [prone.middleware :refer [wrap-exceptions]]
            [org.httpkit.server :only [run-server] :as h]
            [jis.rss :as rss]
            [clojure.data.json :as json]
            ))
(def url "https://blogs.yandex.ru/search.rss")


(defroutes routes
  (GET "/search" {{q :query} :params} 
       (json/write-str (rss/get-hosts-freqs (rss/perform-search url q)))
       )
  (resources "/")
  (not-found "Not Found"))

(def app
  (let [ring-defaults-config (assoc-in 
                              site-defaults 
                              [:security :anti-forgery]
                              {:read-token (fn [req] 
                                             (-> req :session :ring.middleware.anti-forgery/anti-forgery-token))}) 
        prodhandler (wrap-defaults  #'routes site-defaults)
        devhandler (wrap-exceptions (wrap-defaults (reload/wrap-reload #'routes) ring-defaults-config))
        handler (if (env :dev?) 
                  devhandler 
                  prodhandler)
        ]
    handler
    ))

(defn -main [& args]
  (h/run-server app {:port 3000}))
