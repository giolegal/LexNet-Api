package giolegal

import com.microsoft.azure.functions.*
import com.microsoft.azure.functions.annotation.AuthorizationLevel
import com.microsoft.azure.functions.annotation.FunctionName
import com.microsoft.azure.functions.annotation.HttpTrigger
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import org.apache.pdfbox.tools.imageio.ImageIOUtil
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream


/**
 * Render PDF file as JPG stream.
 *
 * In scenarios where we need simple image but we have pdf file instead, rendering PDF seems to be the simplest solution.
 * Function is designed to render simple standard one page files.
 */
class RenderPdf {
    /**
     * This function listens at endpoint "/api/RenderPdf". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/RenderPdf
     * 2. curl {your host}/api/RenderPdf?name=HTTP%20Query
     */
    @FunctionName("RenderPdf")
    fun run(
            @HttpTrigger(name = "req", methods = [HttpMethod.POST], authLevel = AuthorizationLevel.ANONYMOUS) request: HttpRequestMessage<ByteArray>,
            context: ExecutionContext): HttpResponseMessage {
        context.logger.info("Java HTTP trigger processed a request.")

        val body = request.body
        return ByteArrayInputStream(body).use { input ->
            PDDocument.load(input).use {
                val pdfRenderer = PDFRenderer(it)
                val bim = pdfRenderer.renderImageWithDPI(0, 300f, ImageType.RGB)
                val output = ByteArrayOutputStream()
                ImageIOUtil.writeImage(bim, "jpg", output, 300)
                request.createResponseBuilder(HttpStatus.OK).body(output.toByteArray()).header("Content-Type", "image/jpeg").build()
            }
        }
    }
}