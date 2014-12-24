(defproject jis "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/clj"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring "1.3.1"]
                 [ring/ring-defaults "0.1.2"]
                 [prone "0.6.0"]
                 [compojure "1.2.0"]
                 [environ "1.0.0"]
                 [leiningen "2.5.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [http-kit "2.1.18"]
                 [com.github.kyleburton/clj-xpath "1.4.4"]
                 [clojurewerkz/urly "1.0.0"]
                 ]

  :plugins [
            [lein-environ "1.0.0"]
            [lein-ring "0.8.13"]
            ]

  :ring {:handler jis.handler/app}

  :main jis.handler

  :min-lein-version "2.5.0"

  :uberjar-name "jis.jar"

  :profiles {:dev {:repl-options {:init-ns jis.handler
                                  }

                   :dependencies [[ring-mock "0.1.5"]
                                  [ring/ring-devel "1.3.1"]
                                  [midje "1.6.3"]
                                  [pjstadig/humane-test-output "0.6.0"]]

                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :env {:dev? true}
                   }

             :uberjar {
                       :env {:production true}
                       :aot :all
                       :omit-source true
                       }

             :production {:ring {:open-browser? false
                                 :stacktraces?  false
                                 :auto-reload?  false}}})
