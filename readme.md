<h1>A Brokerage Notes Manager</h1>
<h2>The Idea</h2>
<p>It extracts the content of the brokerage notes and balances the operations.</p>
<p>It provides the separation of operations closed by selling or buying an asset or option. It also provides the open operations.</p>
<p>It provides the profit or debt by month or by asset.</p>
<p>Actually, with the correct balance, it is possible to have any statistic, such as profit for any period, any asset/option, or any group of assets/options in any period.</p>
<h2>What is Already Implemented</h2>
<p>Some of the features are already implemented, as described below in the figures:</p>
<br />
<img src="front-end/src/assets/screenshots/send-brokerage-notes.png" width="50%" />
<p>Figure 1: Send brokerage notes in PDF format to be processed.</p>
<br />
<img src="front-end/src/assets/screenshots/dashboard.png" width="50%" />
<p>Figure 2: Dashboard interface which provides some charts.</p>
<br />
<img src="front-end/src/assets/screenshots/brokerage-notes-list.png" width="50%" />
<p>Figure 3: List of brokerage notes.</p>
<br />
<img src="front-end/src/assets/screenshots/operations-list.png" width="50%" />
<p>Figure 4: List of operations extracted from the PDF files.</p>
<h2>What Still Doesn't Work</h2>
<p>Extracting this information from PDF files is a challenge. The Java libraries used have high quality in text extraction from PDFs; however, some inconsistencies occurred during deployment testing. On the other hand, the Python libraries have better accuracy in text extraction but encounter some problems with multitasking processing. Both have been tested, and the Python issue can be resolved more easily than the Java libraries' problem. These issues have already been identified for future implementation. Feel free to fork this project and use it as you wish or try to solve these problems.</p>