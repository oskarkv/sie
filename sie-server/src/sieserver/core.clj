(ns sieserver.core
  (:gen-class)
  (:require [org.httpkit.server :as sv]
            [clojure.data.json :as json]
            [clojure.java.io :as io]))

(def data-dir "uploaded_data/")
(def raw-dir "uploaded_raw/")

(def connected-channels (atom #{}))

(defn keywordize-keys
  "Returns a new map that is like m, but the keys are converted to keywords."
  [m]
  (into {} (map (fn [[k v]] [(keyword k) v])) m))

(defn count-records
  "Counts the number of times records of the form #[A-Z]+ occurrs in the string
   contents. Returns a map from records (strings) to counts."
  [contents]
  (->> (re-seq #"#[A-Z]+" contents)
       (reduce (fn [m r] (assoc m r (inc (get m r 0)))) {})))

(defn read-data-dir-files []
  "Read-strings all the files in the data-dir directory. Returns a seq of the
   results."
  (let [files (filter #(.isFile %) (file-seq (io/file data-dir)))]
    (map (comp read-string slurp) files)))

(defn generate-status-message
  "Generate a status message that is a list of maps with name and record counts
   for the uploaded files, encoded as JSON."
  []
  (json/write-str (read-data-dir-files)))

(defmulti process-message :type)

(defn create-files
  "Saves the uploaded file in the raw-dir directory, and a file with statistics
   about it in data-dir."
  [name contents]
  (let [counts (count-records contents)]
    (spit (str raw-dir name) contents)
    (spit (str data-dir name) (prn-str {:counts counts, :name name}))))

(defmethod process-message "file-upload" [data]
  (let [{:keys [contents name]} data]
    (create-files name contents)
    (generate-status-message)))

(defn delete-files
  "Deletes the uploaded file with the given name, and the file with statistics
   associated with it."
  [name]
  (let [rm #(io/delete-file (str % name))]
    (dorun (map rm [data-dir raw-dir]))))

(defmethod process-message "remove-file" [data]
  (delete-files (:name data))
  (generate-status-message))

(defmethod process-message "connect" [data]
  (generate-status-message))

(defn handler [request]
  (sv/with-channel request channel
    (swap! connected-channels conj channel)
    (sv/on-close
      channel
      (fn [status]
        (swap! connected-channels disj channel)
        (println "channel closed: " status)))
    (sv/on-receive
      channel
      (fn [data]
        (let [response
              (process-message
                (keywordize-keys (json/read-str data)))]
          (dorun (map #(sv/send! % response) @connected-channels)))))))

(defn -main [& args]
  (.mkdir (io/file data-dir))
  (.mkdir (io/file raw-dir))
  (let [port 9090]
    ;; Max WebSocket message size 32 MiB
    (sv/run-server handler {:port port :max-ws (Math/pow 2 25)})
    (println "WebSorket server started on port" port)))

