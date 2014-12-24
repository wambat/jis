(ns jis.core-test
  (:require [midje.sweet :refer :all]
            [org.httpkit.fake :refer :all]
            [jis.rss :as rss]
            
            ))

(facts "XML should parse" 
       (let [xml (slurp "./test/resources/search.rss")
             extracted-links (rss/extract-links xml)
             extracted-hostnames (map rss/extract-hostname extracted-links)
             ]
         (fact "first item link should be http://twitter.com/SyuzannaL/statuses/547703028447322112"
               (first extracted-links) => "http://twitter.com/SyuzannaL/statuses/547703028447322112"
               )
         (fact "last item link should be http://feedproxy.google.com/~r/eaxme/~3/U63_k6D5lhM/"
               (last extracted-links) => "http://feedproxy.google.com/~r/eaxme/~3/U63_k6D5lhM/"
               )
         (fact "first item host should be twitter.com"
               (first extracted-hostnames) => "twitter.com"
               )
         (fact "last item host should be feedproxy.google.com"
               (last extracted-hostnames) => "feedproxy.google.com"
               )
         ))

(facts "RSS should download" 
       (let [responce (slurp "./test/resources/search.rss")
             url "http://fake.url/"
             terms ["scala" "clojure"]]
         
         (with-fake-http [url responce]
           (fact "It should report hostname frequencies"
                 (count (map :body (rss/perform-search url terms))) => 2
                 (rss/get-hosts-freqs (rss/perform-search url terms)) => {"feedproxy.google.com" 1, "twitter.com" 8, "vk.com" 1}

                 )  
           )
         
         ))
