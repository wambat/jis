(ns jis.handler
  (:require 
            [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.anti-forgery :as ring-anti-forgery]
            [clojure.core.async :as async :refer (<! <!! >! >!! put! chan go go-loop)]
            [ring.middleware.reload :as reload]
            [environ.core :refer [env]]
            [prone.middleware :refer [wrap-exceptions]]
            [clojure.pprint :as pp]
            [org.httpkit.server :only [run-server] :as h]
            [org.httpkit.client :as http-client]
            [clj-xpath.core :refer [$x:text*]]
            [clojurewerks.urly.core :refer [url-like]]
            ))

(defn search-yandex [term result-chan]
  "Actually retrieve data from service"
  (let [url "https://blogs.yandex.ru/search.rss"
        options {:timeout 500
                 :query-params {:text term}}
        {:keys [body error]} @(http-client/get url options)
        ]
    (if error
      (>!! result-chan {:term term :body "<error/>"})
      (>!! result-chan {:term term :body body}))
    )
  )

(defn extract-links [x]
  ($x:text* "//item/link" x)
  )

(defn extract-hostname [link]
  (let [url (url-like link)]
    (.getHost url))
  )

(defn perform-search [q]
  "Performs parallel search processing"
  (let [
        result-chan (chan)
        expected-results-count (count q)
        parallels-count (min 10 expected-results-count)
        search-chan (chan parallels-count)]
    ;; Setup proc pool
    (dotimes [f parallels-count]
      (go-loop []
        (let [search-term (<! search-chan)]
          (search-yandex search-term result-chan)
          )
        (recur))  
      )
    
    ;; Load input
    (doseq [term q] 
      (pp/pprint (str "TERM: " term))
      (>!! search-chan term)
      )
    ;; Gather output
    (into [] (for [x (range 0 expected-results-count)]
               (let [result (<!! result-chan)] 
                 (pp/pprint result)
                 (pp/pprint (extract-links result))
                 result
                 )
               ))
    )
  )

(defroutes routes
  (GET "/search" {{q :query} :params} 
       (str "REQ: " q " RESULT: " (perform-search q))
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
