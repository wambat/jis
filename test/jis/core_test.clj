(ns jis.core-test
  (:require [midje.sweet :refer :all]
            [jis.handler :as handler]))

(facts "XML should parse" 
       (let [xml (slurp "./test/resources/search.rss")
             extracted-links (handler/extract-links xml)
             extracted-hostnames (map handler/extract-hostname extracted-links)
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
         ))
