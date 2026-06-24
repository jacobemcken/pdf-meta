(ns app.core
  (:require [reagent.dom :as dom]))

(defn ^:async extract-structured-data
  [^js pdfjs pdf-data]
  (let [^js pdf      (await (.-promise (.getDocument pdfjs #js {:data pdf-data})))
        ^js metadata (await (.getMetadata pdf))]
    #js {:title   (.-Title (.-info metadata))
         :author  (.-Author (.-info metadata))
         :subject (.-Subject (.-info metadata))
         :pages   (.-numPages pdf)}))

(def pdf-data
  (js/atob (str
            "JVBERi0xLjcKCjEgMCBvYmogICUgZW50cnkgcG9pbnQKPDwKICAvVHlwZSAvQ2F0YWxvZwog"
            "IC9QYWdlcyAyIDAgUgo+PgplbmRvYmoKCjIgMCBvYmoKPDwKICAvVHlwZSAvUGFnZXMKICAv"
            "TWVkaWFCb3ggWyAwIDAgMjAwIDIwMCBdCiAgL0NvdW50IDEKICAvS2lkcyBbIDMgMCBSIF0K"
            "Pj4KZW5kb2JqCgozIDAgb2JqCjw8CiAgL1R5cGUgL1BhZ2UKICAvUGFyZW50IDIgMCBSCiAg"
            "L1Jlc291cmNlcyA8PAogICAgL0ZvbnQgPDwKICAgICAgL0YxIDQgMCBSIAogICAgPj4KICA+"
            "PgogIC9Db250ZW50cyA1IDAgUgo+PgplbmRvYmoKCjQgMCBvYmoKPDwKICAvVHlwZSAvRm9u"
            "dAogIC9TdWJ0eXBlIC9UeXBlMQogIC9CYXNlRm9udCAvVGltZXMtUm9tYW4KPj4KZW5kb2Jq"
            "Cgo1IDAgb2JqICAlIHBhZ2UgY29udGVudAo8PAogIC9MZW5ndGggNDQKPj4Kc3RyZWFtCkJU"
            "CjcwIDUwIFRECi9GMSAxMiBUZgooSGVsbG8sIHdvcmxkISkgVGoKRVQKZW5kc3RyZWFtCmVu"
            "ZG9iagoKeHJlZgowIDYKMDAwMDAwMDAwMCA2NTUzNSBmIAowMDAwMDAwMDEwIDAwMDAwIG4g"
            "CjAwMDAwMDAwNzkgMDAwMDAgbiAKMDAwMDAwMDE3MyAwMDAwMCBuIAowMDAwMDAwMzAxIDAw"
            "MDAwIG4gCjAwMDAwMDAzODAgMDAwMDAgbiAKdHJhaWxlcgo8PAogIC9TaXplIDYKICAvUm9v"
            "dCAxIDAgUgo+PgpzdGFydHhyZWYKNDkyCiUlRU9G")))

(comment
  (.catch
   (.then (extract-structured-data (.-pdfjsLib js/globalThis) pdf-data)
          #(js/console.log %))
   #(js/console.error %))
  )

(defn read-file-using
  "Read file using `process-fn`."
  [file process-fn]
  (let [file-reader (js/FileReader.)]
    (set! (.-onload file-reader) #(process-fn (-> % .-target .-result js/Uint8Array.)))
    (.readAsArrayBuffer file-reader file)))

; https://readymadeui.com/tailwind/component/file-upload-container
(defn file-upload
  [file-selected-fn]
  [:label
   {:for "uploadfile"
    :class "bg-white text-slate-600 font-semibold text-sm rounded-md max-w-sm h-48 flex flex-col items-center justify-center cursor-pointer border-2 border-slate-300 border-dashed mx-auto mt-6 focus-within:ring-2 focus-within:ring-blue-500 dark:bg-neutral-900 dark:text-slate-300 dark:border-neutral-700"}
   [:svg
    {:xmlns "http://www.w3.org/2000/svg"
     :class "size-10 mb-4 fill-gray-400"
     :viewBox "0 0 32 32"
     :aria-hidden "true"}
    [:path
     {:d "M23.75 11.044a7.99 7.99 0 0 0-15.5-.009A8 8 0 0 0 9 27h3a1 1 0 0 0 0-2H9a6 6 0 0 1-.035-12 1.038 1.038 0 0 0 1.1-.854 5.991 5.991 0 0 1 11.862 0A1.08 1.08 0 0 0 23 13a6 6 0 0 1 0 12h-3a1 1 0 0 0 0 2h3a8 8 0 0 0 .75-15.956z"}]
    [:path
     {:d "M20.293 19.707a1 1 0 0 0 1.414-1.414l-5-5a1 1 0 0 0-1.414 0l-5 5a1 1 0 0 0 1.414 1.414L15 16.414V29a1 1 0 0 0 2 0V16.414z"}]]
   "Load PDF file"
   [:input {:type "file" :id "uploadfile" :class "sr-only"
            :on-change (fn [e] (file-selected-fn (-> e .-target .-files first)))}]
   [:p {:class "text-xs font-normal text-slate-400 text-center mt-2"}
    "PNG, JPG SVG, WEBP, and GIF are Allowed."]])

(defn ^:dev/after-load start
  []
  (dom/render [file-upload (fn [file]
                             (read-file-using file (fn [file-data]
                                                     (.catch
                                                      (.then (extract-structured-data (.-pdfjsLib js/globalThis) file-data)
                                                             #(js/console.log %))
                                                      #(js/console.error %)))))]
              (.getElementById js/document "app")))

(defn ^:export init []
  (if-not js/Worker
    (js/console.error "Web Workers not supported.")
    (set! (.-workerSrc (.-GlobalWorkerOptions (.-pdfjsLib js/globalThis)))
          "https://cdn.jsdelivr.net/npm/pdfjs-dist@6.0.227/build/pdf.worker.mjs"))
  (start))
