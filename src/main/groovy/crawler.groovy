import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook

def executarCrawler() {
    baixarVersaoMaisRecente()
    coletarDadosHistorico()
    baixarTabelaErros()
}

def downloadFile(String fileUrl, String destinationPath) {
    println "Baixando o arquivo de: $fileUrl"

    try {
        URL url = new URL(fileUrl)
        HttpURLConnection connection = (HttpURLConnection) url.openConnection()
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
        connection.connect()

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream inputStream = connection.getInputStream()
            FileOutputStream outputStream = new FileOutputStream(destinationPath)

            byte[] buffer = new byte[2048]
            int bytesRead
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            inputStream.close()
            outputStream.close()
            println "Arquivo baixado com sucesso para $destinationPath"
        } else {
            println "Erro ao conectar. Código de resposta: " + connection.getResponseCode()
        }
    } catch (Exception e) {
        println "Erro durante o download: ${e.message}"
    }
}


def baixarVersaoMaisRecente() {
    println "Baixando a versão mais recente do TISS..."
    // Passo 1: Acessar a página inicial do Padrão TISS
    String tissMainPage = "https://www.gov.br/ans/pt-br/assuntos/prestadores/padrao-para-troca-de-informacao-de-saude-suplementar-2013-tiss"
    URL tissMainUrl = new URL(tissMainPage)
    HttpURLConnection tissMainConnection = (HttpURLConnection) tissMainUrl.openConnection()
    tissMainConnection.setRequestMethod("GET")
    tissMainConnection.connect()

    int tissMainResponseCode = tissMainConnection.getResponseCode()
    if (tissMainResponseCode == 200) {
        println "Página inicial do TISS carregada com sucesso!"
        InputStream tissMainInputStream = tissMainConnection.getInputStream()
        String tissMainContent = tissMainInputStream.text
        Document tissMainDocument = Jsoup.parse(tissMainContent)


        Elements links = tissMainDocument.select("a[href]")
        for (Element link : links) {
            String linkHref = link.attr("href")
            String linkText = link.text()


            if (linkText.contains("Setembro/2024")) {
                println "Link da versão Setembro/2024 encontrado: $linkHref - Texto: $linkText"

                if (!linkHref.startsWith("http")) {
                    linkHref = "https://www.gov.br" + linkHref
                }

                acessarPaginaVersaoTISS(linkHref)
            }
        }
    } else {
        println "Falha ao carregar a página inicial do TISS. Código de resposta: ${tissMainResponseCode}"
    }
}


def acessarPaginaVersaoTISS(String versionLink) {
    println "Acessando a página da versão Setembro/2024: $versionLink"

    URL versionUrl = new URL(versionLink)
    HttpURLConnection versionConnection = (HttpURLConnection) versionUrl.openConnection()
    versionConnection.setRequestMethod("GET")
    versionConnection.connect()

    int versionResponseCode = versionConnection.getResponseCode()
    if (versionResponseCode == 200) {
        println "Página da versão do TISS carregada com sucesso!"
        InputStream versionInputStream = versionConnection.getInputStream()
        String versionContent = versionInputStream.text
        Document versionDocument = Jsoup.parse(versionContent)

        Elements versionLinks = versionDocument.select("a[href]")
        for (Element link : versionLinks) {
            String linkHref = link.attr("href")
            String linkText = link.text()

            if (linkText.contains("Componente de Comunicação")) {
                println "Link do Componente de Comunicação encontrado: $linkHref - Texto: $linkText"


                if (!linkHref.startsWith("http")) {
                    linkHref = "https://www.gov.br" + linkHref
                }

                baixarArquivo(linkHref)
            }
        }
    } else {
        println "Falha ao carregar a página da versão do TISS. Código de resposta: ${versionResponseCode}"
    }
}


def baixarArquivo(String fileUrl) {
    println "Iniciando o download do arquivo: $fileUrl"

    try {
        URL url = new URL(fileUrl)
        HttpURLConnection connection = (HttpURLConnection) url.openConnection()

        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
        connection.connect()

        int responseCode = connection.getResponseCode()
        if (responseCode == 200) {
            println "Conexão bem-sucedida! Iniciando o download..."

            // Criar a pasta de destino, se não existir
            String downloadsPath = new File(".").getAbsolutePath() + "/Downloads"
            File downloadDir = new File(downloadsPath)
            if (!downloadDir.exists()) {
                boolean created = downloadDir.mkdirs()
                if (created) {
                    println "Pasta Downloads criada."
                } else {
                    println "Falha ao criar a pasta Downloads."
                }
            } else {
                println "Pasta Downloads já existe."
            }

            String destinationPath = downloadsPath + "/Componente_de_Comunicacao.zip"
            InputStream inputStream = connection.getInputStream()
            FileOutputStream outputStream = new FileOutputStream(new File(destinationPath))

            byte[] buffer = new byte[2048]
            int bytesRead
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            inputStream.close()
            outputStream.close()

            println "Arquivo baixado com sucesso: $destinationPath"
        } else {
            println "Falha ao conectar. Código de resposta: ${responseCode}"
        }
    } catch (Exception e) {
        println "Erro durante o download: ${e.message}"
    }
}



def coletarDadosHistorico() {
    println "Acessando a página de Histórico das versões dos Componentes do Padrão TISS..."

    String urlHistorico = "https://www.gov.br/ans/pt-br/assuntos/prestadores/padrao-para-troca-de-informacao-de-saude-suplementar-2013-tiss/padrao-tiss-historico-das-versoes-dos-componentes-do-padrao-tiss"
    Document doc = Jsoup.connect(urlHistorico).get()

    Elements linhasTabela = doc.select("table tbody tr")  // Ajustar o seletor conforme a estrutura da tabela


    Workbook workbook = new XSSFWorkbook()
    Sheet sheet = workbook.createSheet("Histórico TISS")

    Row header = sheet.createRow(0)
    header.createCell(0).setCellValue("Competência")
    header.createCell(1).setCellValue("Publicação")
    header.createCell(2).setCellValue("Início de Vigência")

    int rowNum = 1

    for (Element linha : linhasTabela) {
        // Extrair os dados de cada coluna (Competência, Publicação, Início de Vigência)
        String competencia = linha.select("td:nth-child(1)").text() // 1ª coluna: Competência
        String publicacao = linha.select("td:nth-child(2)").text()   // 2ª coluna: Publicação
        String inicioVigencia = linha.select("td:nth-child(3)").text() // 3ª coluna: Início de Vigência

        if (competencia >= "2016-01") {
            println "Competência: $competencia, Publicação: $publicacao, Início de Vigência: $inicioVigencia"

            Row row = sheet.createRow(rowNum++)
            row.createCell(0).setCellValue(competencia)
            row.createCell(1).setCellValue(publicacao)
            row.createCell(2).setCellValue(inicioVigencia)
        }
    }

    String excelFilePath = "./Downloads/Historico_TISS.xls"
    FileOutputStream fileOut = new FileOutputStream(excelFilePath)
    workbook.write(fileOut)
    fileOut.close()
    workbook.close()

    println "Dados salvos no arquivo Excel: $excelFilePath"
}
coletarDadosHistorico()


def baixarTabelaErros() {
    println "Acessando a página para baixar a Tabela de Erros..."

    String urlErros = "https://www.gov.br/ans/pt-br/arquivos/assuntos/prestadores/padrao-para-troca-de-informacao-de-saude-suplementar-tiss/padrao-tiss-tabelas-relacionadas/Tabelaerrosenvioparaanspadraotiss__1_.xlsx"
    String destino = "./Downloads/Historico_TISS.xls"

    downloadFile(urlErros, destino)
}

executarCrawler()
