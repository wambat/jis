(ns jis.rss
  (:require 
            [clojure.core.async :as async :refer (<! <!! >! >!! put! chan go go-loop)]
            [clojure.pprint :as pp]
            [org.httpkit.client :as http-client]
            [clj-xpath.core :refer [$x:text*]]
            [org.bovinegenius.exploding-fish :as uri]
            ))

(defn search-rss [u term result-chan]
  "Actually retrieve data from service"
  (let [
        options {:timeout 1500
                 :query-params {:text term}}
        {:keys [body error]} @(http-client/get u options)
        ]
    (if error
      (>!! result-chan {:term term :body "<error/>"})
      (>!! result-chan {:term term :body body}))
    )
  )

(defn extract-links [x]
  (into [] ($x:text* "//item/link" x))
  )

(defn extract-hostname [link]
  (let [xplode (uri/uri link)]
    (:host xplode))
  )

(defn get-hosts-freqs [resps]
  (frequencies 
   (map extract-hostname 
        (distinct 
         (mapcat 
          (fn [responce] 
            (take 10 (-> responce :body extract-links))) 
          resps))))
  )

(defn jsonyfy [resp]
  resp
  )

(defn perform-search [u q]
  "Performs parallel search processing"
  (let [
        result-chan (chan)
        expected-results-count (count q)
        parallels-count (min 3 expected-results-count)
        search-chan (chan parallels-count)]
    ;; Setup proc pool
    (dotimes [f parallels-count]
      (go-loop []
        (let [search-term (<! search-chan)]
          (search-rss u search-term result-chan)
          )
        (recur))  
      )
    
    ;; Load input
    (doseq [term q] 
      (>!! search-chan term)
      )
    ;; Gather output
    (into [] (for [x (range 0 expected-results-count)]
               (let [result (<!! result-chan)] 
                 result
                 )
               ))
    )
  )
