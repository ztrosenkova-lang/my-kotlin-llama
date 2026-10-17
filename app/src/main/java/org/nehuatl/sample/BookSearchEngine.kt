package org.nehuatl.sample

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.TextField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.FSDirectory
import java.io.File

class BookSearchEngine(private val indexDir: File) {

    private val analyzer = StandardAnalyzer()

    fun clearIndex() {
        indexDir.deleteRecursively()
        indexDir.mkdirs()
    }

    fun indexBooks(books: List<Triple<String, String, String>>) {
        indexDir.mkdirs()
        val dir = FSDirectory.open(indexDir.toPath())
        val config = IndexWriterConfig(analyzer)
        config.openMode = IndexWriterConfig.OpenMode.CREATE
        val writer = IndexWriter(dir, config)
        try {
            for ((title, content, bookFile) in books) {
                val doc = Document()
                doc.add(TextField("title", title, Field.Store.YES))
                doc.add(TextField("content", content, Field.Store.YES))
                doc.add(TextField("file", bookFile, Field.Store.YES))
                writer.addDocument(doc)
            }
        } finally {
            writer.close()
            dir.close()
        }
    }

    fun search(query: String, limit: Int = 50): List<Pair<String, String>> {
        if (!indexDir.exists()) return emptyList()
        val dir = FSDirectory.open(indexDir.toPath())
        val reader = DirectoryReader.open(dir)
        try {
            val searcher = IndexSearcher(reader)
            val parser = QueryParser("content", analyzer)
            val q = parser.parse(QueryParser.escape(query))
            val hits = searcher.search(q, limit)
            val results = mutableListOf<Pair<String, String>>()
            for (hit in hits.scoreDocs) {
                val doc = searcher.storedFields().document(hit.doc)
                val title = doc.get("title") ?: ""
                val content = doc.get("content") ?: ""
                val idx = content.indexOf(query, ignoreCase = true)
                val snippet = if (idx >= 0) {
                    val start = (idx - 50).coerceAtLeast(0)
                    val end = (idx + query.length + 100).coerceAtMost(content.length)
                    "..." + content.substring(start, end).replace("\n", " ") + "..."
                } else {
                    content.take(150).replace("\n", " ") + "..."
                }
                results.add(title to snippet)
            }
            return results
        } finally {
            reader.close()
            dir.close()
        }
    }
}
