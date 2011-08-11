(ns resque-clojure.worker
  (:use [clojure.string :only [split]]))

(defn lookup-fn [namespaced-fn]
  (let [[namespace fun] (split namespaced-fn #"/")]
    (ns-resolve (symbol namespace) (symbol fun))))

(defn work-on [state job queue]
  (let [{namespaced-fn :class args :args} job]
    (try
      (apply (lookup-fn namespaced-fn) args)
      {:result :pass :job job :queue queue}
      (catch Exception e
        {:result :error :exception e :job job :queue queue}))))

(defn name [queues]
  (let [pid-host (.getName (java.lang.management.ManagementFactory/getRuntimeMXBean))
        [pid hostname] (split pid-host #"@")
        qs (apply str (interpose "," queues))]
    (str hostname ":" pid ":" qs)))
