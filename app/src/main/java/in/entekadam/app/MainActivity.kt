                val id = dao.insertPayment(Payment(debtId = debt.id, amountPaise = amount, date = date, method = method, notes = note))
                dao.insertAudit(AuditEntry(entityType = "PAYMENT", entityId = id, action = "RECORDED", details = "${money(amount)} · $method")); payDebt = null
            }
        }
    })
    if (payOverLimit) AlertDialog(onDismissRequest = { payOverLimit = false; pendingPayment = null }, title = { Text(tr("Amount exceeds balance", "ബാക്കി തുകയേക്കാൾ കൂടുതലാണ്")) }, text = { Text(tr("This payment is greater than the remaining balance. Do you explicitly want to record the full amount anyway?", "ഈ പേയ്മെന്റ് ബാക്കി തുകയേക്കാൾ കൂടുതലാണ്. മുഴുവൻ തുകയും രേഖപ്പെടുത്തണമെന്ന് ഉറപ്പാണോ?")) }, confirmButton = { TextButton(onClick = { scope.launch { pendingPayment?.let { draft -> val id = dao.insertPayment(draft); dao.insertAudit(AuditEntry(entityType = "PAYMENT", entityId = id, action = "RECORDED_OVERPAYMENT_CONFIRMED", details = money(draft.amountPaise))) }; pendingPayment = null; payOverLimit = false; payDebt = null } }) { Text(tr("Record anyway", "എന്നിട്ടും രേഖപ്പെടുത്തുക")) } }, dismissButton = { TextButton(onClick = { pendingPayment = null; payOverLimit = false }) { Text(tr("Go back", "തിരികെ പോകുക")) } })
    if (showStatement) StatementDialog(statementText, ::tr, onChange = { statementText = it }, onDismiss = { showStatement = false }, onShare = { shareText(context, statementText); showStatement = false }, onWhatsApp = { shareText(context, statementText, true); showStatement = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun HomeScreen(t: List<Long>, people: List<Person>, debts: List<Debt>, payments: List<Payment>, search: String, onSearch: (String)->Unit, tr: (String,String)->String, onAdd: (String)->Unit, onPerson: (Person)->Unit, onPay: (Debt)->Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
        item { Spacer(Modifier.height(4.dp)); Text(tr("Your money overview", "നിങ്ങളുടെ പണത്തിന്റെ കണക്ക്"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        item { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { SummaryCard(tr("To receive", "എനിക്ക് കിട്ടാനുള്ളത്"), money(t[0]), Color(0xFF168653), Modifier.weight(1f)); SummaryCard(tr("To pay", "ഞാൻ കൊടുക്കാനുള്ളത്"), money(t[1]), Color(0xFFB66A25), Modifier.weight(1f)) }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { SummaryCard(tr("Total received", "ആകെ തിരികെ കിട്ടിയത്"), money(t[2]), Color(0xFF397CB5), Modifier.weight(1f)); SummaryCard(tr("Total paid", "ആകെ തിരികെ കൊടുത്തത്"), money(t[3]), Color(0xFF8B64B5), Modifier.weight(1f)) }
        } }
        item { Card(colors = CardDefaults.cardColors(containerColor = Pale), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(tr("Net outstanding", "ആകെ ബാക്കി"), color = Green); Text(money(t[0] - t[1]), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Green) } } }
        item { Text(tr("Quick actions", "പെട്ടെന്നുള്ള പ്രവർത്തനങ്ങൾ"), fontWeight = FontWeight.Bold); Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { Button(onClick = { onAdd("RECEIVE") }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(4.dp)); Text(tr("Lend money", "പണം കൊടുക്കുക")) }; OutlinedButton(onClick = { onAdd("PAY") }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.CallReceived, null); Spacer(Modifier.width(4.dp)); Text(tr("Borrow money", "പണം കടം വാങ്ങുക")) } } }
        item { OutlinedTextField(search, onSearch, modifier = Modifier.fillMaxWidth(), placeholder = { Text(tr("Search people...", "പേര് തിരയുക...")) }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true) }
        item { Text(tr("Recent people", "സമീപകാല ആളുകൾ"), fontWeight = FontWeight.Bold) }
        val filtered = people.filter { it.name.contains(search, true) || it.phone.contains(search) }.take(6)
        items(filtered, key = { it.id }) { p -> PersonRow(p, debts.filter { it.personId == p.id }, payments, tr, { onPerson(p) }, { d -> onPay(d) }) }
        if (filtered.isEmpty()) item { EmptyState(tr("No records yet. Add your first loan.", "ഇതുവരെ വിവരങ്ങളില്ല. ആദ്യ കടം ചേർക്കുക.")) }
    }
}

@Composable private fun SummaryCard(label: String, amount: String, tint: Color, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(1.dp)) { Column(Modifier.padding(13.dp)) { Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(8.dp)); Text(amount, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = tint) } }
}

@Composable private fun PeopleScreen(people: List<Person>, debts: List<Debt>, payments: List<Payment>, search: String, onSearch: (String)->Unit, tr: (String,String)->String, onAdd: (String)->Unit, onPerson: (Person)->Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(tr("People & accounts", "ആളുകളും അക്കൗണ്ടുകളും"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp)); OutlinedTextField(search, onSearch, Modifier.fillMaxWidth(), placeholder = { Text(tr("Search name or phone", "പേര് അല്ലെങ്കിൽ ഫോൺ തിരയുക")) }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true)
        Spacer(Modifier.height(10.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button(onClick = { onAdd("RECEIVE") }, modifier = Modifier.weight(1f)) { Text(tr("To receive +", "കിട്ടാനുള്ളത് +")) }; OutlinedButton(onClick = { onAdd("PAY") }, modifier = Modifier.weight(1f)) { Text(tr("To pay +", "കൊടുക്കാനുള്ളത് +")) } }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
            items(people.filter { it.name.contains(search, true) || it.phone.contains(search) }, key = { it.id }) { p -> PersonRow(p, debts.filter { it.personId == p.id }, payments, tr, { onPerson(p) }, {}) }
            if (people.isEmpty()) item { EmptyState(tr("No people added", "ആരെയും ചേർത്തിട്ടില്ല")) }
        }
    }
}

@Composable private fun PersonRow(p: Person, debts: List<Debt>, payments: List<Payment>, tr: (String,String)->String, onClick: ()->Unit, onPay: (Debt)->Unit) {
    val paid = payments.filter { pay -> debts.any { it.id == pay.debtId } }.sumOf { it.amountPaise }
    val total = debts.sumOf { it.amountPaise }; val balance = (total-paid).coerceAtLeast(0)
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(12.dp), color = Pale, modifier = Modifier.size(42.dp)) { Box(contentAlignment = Alignment.Center) { Text(p.name.take(1).uppercase(), color = Green, fontWeight = FontWeight.Bold) } }
        Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(p.name, fontWeight = FontWeight.SemiBold); Text(if (p.phone.isBlank()) if (p.side == "RECEIVE") tr("Money to receive", "കിട്ടാനുള്ളത്") else tr("Money to pay", "കൊടുക്കാനുള്ളത്") else p.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Column(horizontalAlignment = Alignment.End) { Text(money(balance), fontWeight = FontWeight.Bold, color = if (p.side == "RECEIVE") Green else Color(0xFFB66A25)); Text(if (p.side == "RECEIVE") tr("To receive", "കിട്ടാനുള്ളത്") else tr("To pay", "കൊടുക്കാനുള്ളത്"), style = MaterialTheme.typography.labelSmall) }
    } }
}

@Composable private fun PersonDetails(p: Person, debts: List<Debt>, payments: List<Payment>, dao: FinanceDao, language: String, onBack: ()->Unit, onAddDebt: ()->Unit, onPay: (Debt)->Unit, onShare: ()->Unit, tr: (String,String)->String) {
    val scope = rememberCoroutineScope(); val allPayments = payments.filter { pay -> debts.any { it.id == pay.debtId } }
    val total = debts.sumOf { it.amountPaise }; val paid = allPayments.sumOf { it.amountPaise }; val balance = (total-paid).coerceAtLeast(0)
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }; Text(p.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); IconButton(onClick = onShare) { Icon(Icons.Default.Share, tr("Share", "പങ്കിടുക")) } }
        Text(p.phone.ifBlank { tr("No phone number", "ഫോൺ നമ്പർ നൽകിയിട്ടില്ല") }, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(Modifier.fillMaxWidth().padding(vertical = 12.dp), colors = CardDefaults.cardColors(containerColor = Pale), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(16.dp)) { Text(tr("Remaining balance", "ബാക്കി തുക"), color = Green); Text(money(balance), style = MaterialTheme.typography.headlineMedium, color = Green, fontWeight = FontWeight.Bold); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("${tr("Total", "ആകെ")}: ${money(total)}"); Text("${tr("Repaid", "തിരിച്ചടച്ചത്")}: ${money(paid)}") } } }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button(onClick = onAddDebt, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Add, null); Text(tr("Add debt", "കടം ചേർക്കുക")) }; OutlinedButton(onClick = onShare, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Share, null); Text(tr("Share statement", "കണക്ക് പങ്കിടുക")) } }
        Text(tr("Debt entries", "കടം നൽകിയ വിവരങ്ങൾ"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 14.dp, bottom = 8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(debts, key = { it.id }) { d ->
                val repaid = payments.filter { it.debtId == d.id }.sumOf { it.amountPaise }; val remain = (d.amountPaise-repaid).coerceAtLeast(0)
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Column(Modifier.padding(13.dp)) { Row { Column(Modifier.weight(1f)) { Text(money(d.amountPaise), fontWeight = FontWeight.Bold); Text(dateText(d.date), style = MaterialTheme.typography.labelSmall) }; Text(money(remain), color = Green, fontWeight = FontWeight.Bold) }; if (d.description.isNotBlank()) Text(d.description, style = MaterialTheme.typography.bodySmall); if (d.dueDate != null) Text("${tr("Due", "അവസാന തീയതി")}: ${dateText(d.dueDate)}", style = MaterialTheme.typography.labelSmall); if (remain > 0) TextButton(onClick = { onPay(d) }) { Text(tr("Record payment", "പേയ്മെന്റ് ചേർക്കുക")) }; payments.filter { it.debtId == d.id }.forEach { pay -> Text("↳ ${dateText(pay.date)} · ${money(pay.amountPaise)} · ${pay.method}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
            }
            if (debts.isEmpty()) item { EmptyState(tr("No debt entries", "കടം വിവരങ്ങളില്ല")) }
        }
    }
}

@Composable private fun TransactionsScreen(people: List<Person>, debts: List<Debt>, payments: List<Payment>, search: String, onSearch: (String)->Unit, tr: (String,String)->String) {
    val pmap = people.associateBy { it.id }; val dmap = debts.associateBy { it.id }
    val entries = buildList {
        debts.forEach { d -> pmap[d.personId]?.let { add(Triple(d.date, it.name, "${if (it.side == "RECEIVE") "Loan given" else "Loan borrowed"} · ${money(d.amountPaise)}")) } }
        payments.forEach { pay -> dmap[pay.debtId]?.let { d -> pmap[d.personId]?.let { add(Triple(pay.date, it.name, "${tr("Payment", "പേയ്മെന്റ്")} · ${money(pay.amountPaise)} · ${pay.method}")) } } }
    }.filter { it.second.contains(search, true) }.sortedByDescending { it.first }
    Column(Modifier.fillMaxSize().padding(16.dp)) { Text(tr("Transaction history", "ഇടപാട് ചരിത്രം"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Spacer(Modifier.height(12.dp)); OutlinedTextField(search, onSearch, Modifier.fillMaxWidth(), placeholder = { Text(tr("Search person", "പേര് തിരയുക")) }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true); LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 12.dp)) { items(entries) { e -> Card(Modifier.fillMaxWidth()) { Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(e.second, fontWeight = FontWeight.SemiBold); Text(e.third, style = MaterialTheme.typography.bodySmall) }; Text(dateText(e.first), style = MaterialTheme.typography.labelSmall) } } }; if (entries.isEmpty()) item { EmptyState(tr("No transactions found", "ഇടപാടുകളൊന്നും കണ്ടെത്തിയില്ല")) } } }
}

@Composable private fun ReportsScreen(t: List<Long>, people: List<Person>, debts: List<Debt>, payments: List<Payment>, tr: (String,String)->String, onPdf: ()->Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) { Text(tr("Reports & statistics", "റിപ്പോർട്ടുകളും കണക്കുകളും"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Spacer(Modifier.height(12.dp)); listOf(tr("Total money lent", "ആകെ കടം കൊടുത്തത്") to t[4], tr("Total money borrowed", "ആകെ കടം വാങ്ങിയത്") to t[5], tr("Total received", "ആകെ തിരികെ കിട്ടിയത്") to t[2], tr("Total paid", "ആകെ തിരികെ കൊടുത്തത്") to t[3], tr("Outstanding receivables", "കിട്ടാനുള്ള ബാക്കി") to t[0], tr("Outstanding payables", "കൊടുക്കാനുള്ള ബാക്കി") to t[1]).forEach { (label, value) -> Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) { Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, modifier = Modifier.weight(1f)); Text(money(value), fontWeight = FontWeight.Bold) } } }; Button(onClick = onPdf, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.PictureAsPdf, null); Spacer(Modifier.width(8.dp)); Text(tr("Export PDF report", "PDF റിപ്പോർട്ട് എടുക്കുക")) }; Text(tr("Person-wise balances", "വ്യക്തി തിരിച്ചുള്ള ബാക്കി"), fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 14.dp)); LazyColumn { items(people) { p -> val pd = debts.filter { it.personId == p.id }; val bal = pd.sumOf { it.amountPaise } - payments.filter { pay -> pd.any { it.id == pay.debtId } }.sumOf { it.amountPaise }; Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(p.name); Text(money(bal.coerceAtLeast(0)), fontWeight = FontWeight.SemiBold) } } } }
}

@Composable private fun SettingsScreen(language: String, dark: Boolean, onLanguage: (String)->Unit, onDark: (Boolean)->Unit, tr: (String,String)->String, dao: FinanceDao, db: AppDatabase) {
    val context = LocalContext.current; val scope = rememberCoroutineScope(); var status by remember { mutableStateOf("") }; var confirmRestore by remember { mutableStateOf(false) }
    val createBackup = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch { runCatching { val data = BackupData(people = dao.allPeople(), debts = dao.allDebts(), payments = dao.allPayments(), audit = dao.allAudit()); context.contentResolver.openOutputStream(uri)!!.bufferedWriter().use { it.write(BackupManager.encode(data)) }; status = tr("Backup saved", "ബാക്കപ്പ് സേവ് ചെയ്തു") }.onFailure { status = it.message ?: "Backup failed" } }
    }
    val openBackup = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch { runCatching { val json = context.contentResolver.openInputStream(uri)!!.bufferedReader().use { it.readText() }; val data = BackupManager.decode(json); db.withTransaction { dao.clearPayments(); dao.clearDebts(); dao.clearPeople(); dao.clearAudit(); dao.insertPeople(data.people); dao.insertDebts(data.debts); dao.insertPayments(data.payments); if (data.audit.isNotEmpty()) dao.insertAuditItems(data.audit) }; status = tr("Backup restored", "ബാക്കപ്പ് പുനഃസ്ഥാപിച്ചു") }.onFailure { status = "${tr("Restore failed", "പുനഃസ്ഥാപിക്കൽ പരാജയപ്പെട്ടു")}: ${it.message}" } }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text(tr("Settings", "ക്രമീകരണങ്ങൾ"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(tr("Language", "ഭാഷ"), fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 18.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { FilterChip(selected = language == "ml", onClick = { onLanguage("ml") }, label = { Text("മലയാളം") }); FilterChip(selected = language == "en", onClick = { onLanguage("en") }, label = { Text("English") }) }
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { Text(tr("Dark theme", "ഡാർക്ക് തീം"), modifier = Modifier.weight(1f)); Switch(dark, onDark) }
        HorizontalDivider(); Text(tr("Backup & restore", "ബാക്കപ്പ് & പുനഃസ്ഥാപിക്കൽ"), fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 14.dp)); Button(onClick = { createBackup.launch("ente-kadam-backup-${System.currentTimeMillis()}.json") }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Backup, null); Spacer(Modifier.width(8.dp)); Text(tr("Create JSON backup", "JSON ബാക്കപ്പ് സൃഷ്ടിക്കുക")) }; OutlinedButton(onClick = { confirmRestore = true }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Restore, null); Spacer(Modifier.width(8.dp)); Text(tr("Restore from backup", "ബാക്കപ്പിൽ നിന്ന് പുനഃസ്ഥാപിക്കുക")) }
        if (status.isNotBlank()) Text(status, color = Green, modifier = Modifier.padding(8.dp)); HorizontalDivider(Modifier.padding(vertical = 12.dp)); Text(tr("Privacy", "സ്വകാര്യത"), fontWeight = FontWeight.SemiBold); Text(tr("Your financial records are stored locally on this device. The app does not require an account or internet connection.", "നിങ്ങളുടെ സാമ്പത്തിക വിവരങ്ങൾ ഈ ഫോണിൽ തന്നെയാണ് സൂക്ഷിക്കുന്നത്. അക്കൗണ്ടോ ഇന്റർനെറ്റോ ആവശ്യമില്ല."), style = MaterialTheme.typography.bodyMedium); Text("Ente Kadam · 1.0.0", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 24.dp))
    }
    if (confirmRestore) AlertDialog(onDismissRequest = { confirmRestore = false }, title = { Text(tr("Replace existing data?", "നിലവിലെ വിവരങ്ങൾ മാറ്റണോ?")) }, text = { Text(tr("Restoring a backup replaces all current records. Export a backup of current data first if you need it.", "ബാക്കപ്പ് പുനഃസ്ഥാപിക്കുമ്പോൾ നിലവിലെ എല്ലാ വിവരങ്ങളും മാറ്റപ്പെടും. ആവശ്യമെങ്കിൽ ആദ്യം നിലവിലെ വിവരങ്ങൾ ബാക്കപ്പ് ചെയ്യുക.")) }, confirmButton = { TextButton(onClick = { confirmRestore = false; openBackup.launch(arrayOf("application/json", "text/json", "text/plain")) }) { Text(tr("Continue", "തുടരുക")) } }, dismissButton = { TextButton(onClick = { confirmRestore = false }) { Text(tr("Cancel", "റദ്ദാക്കുക")) } })
}

@Composable private fun AddDebtDialog(side: String, existing: Person?, tr: (String,String)->String, onDismiss: ()->Unit, onSave: (String,String,Long,Long,String,Double?,Long?)->Unit) {
    var name by remember(existing) { mutableStateOf(existing?.name ?: "") }; var phone by remember(existing) { mutableStateOf(existing?.phone ?: "") }; var amount by remember { mutableStateOf("") }; var note by remember { mutableStateOf("") }; var interest by remember { mutableStateOf("") }; var date by remember { mutableLongStateOf(System.currentTimeMillis()) }; var due by remember { mutableStateOf("") }; var error by remember { mutableStateOf("") }
    val context = LocalContext.current
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (existing == null) tr(if (side == "RECEIVE") "Add money to receive" else "Add money to pay", if (side == "RECEIVE") "എനിക്ക് കിട്ടാനുള്ളത് ചേർക്കുക" else "ഞാൻ കൊടുക്കാനുള്ളത് ചേർക്കുക") else tr("Add another debt", "കൂടുതൽ കടം ചേർക്കുക")) }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text(tr("Person name *", "പേര് *")) }, singleLine = true)
            OutlinedTextField(phone, { phone = it }, label = { Text(tr("Mobile number", "മൊബൈൽ നമ്പർ")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true)
            OutlinedTextField(amount, { amount = it }, label = { Text(tr("Amount (₹) *", "തുക (₹) *")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
            OutlinedButton(onClick = { val c = Calendar.getInstance().apply { timeInMillis = date }; DatePickerDialog(context, { _, y, m, d -> c.set(y,m,d); date = c.timeInMillis }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show() }, modifier = Modifier.fillMaxWidth()) { Text("${tr("Date", "തീയതി")}: ${dateText(date)}") }
            OutlinedTextField(note, { note = it }, label = { Text(tr("Description / notes", "വിവരണം / കുറിപ്പുകൾ")) })
            OutlinedTextField(interest, { interest = it }, label = { Text(tr("Interest rate % (optional)", "പലിശ നിരക്ക് % (ഐച്ഛികം)")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
            OutlinedTextField(due, { due = it }, label = { Text(tr("Due date DD-MM-YYYY (optional)", "അവസാന തീയതി DD-MM-YYYY")) }, singleLine = true)
            if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
        }
    }, confirmButton = { TextButton(onClick = { val parsed = parsePaise(amount); val dueTime = if (due.isBlank()) null else runCatching { java.text.SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(due)?.time }.getOrNull(); if (name.isBlank() || parsed == null || parsed <= 0) error = tr("Enter a name and valid amount", "പേരും ശരിയായ തുകയും നൽകുക") else if (due.isNotBlank() && dueTime == null) error = tr("Enter due date as DD-MM-YYYY", "തീയതി DD-MM-YYYY രൂപത്തിൽ നൽകുക") else onSave(name.trim(), phone.trim(), parsed, date, note.trim(), interest.toDoubleOrNull(), dueTime) }) { Text(tr("Save", "സേവ്")) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(tr("Cancel", "റദ്ദാക്കുക")) } })
}

@Composable private fun PaymentDialog(debt: Debt, tr: (String,String)->String, onDismiss: ()->Unit, onSave: (Long,Long,String,String)->Unit) {
    var amount by remember { mutableStateOf("") }; var method by remember { mutableStateOf("Cash") }; var note by remember { mutableStateOf("") }; var date by remember { mutableLongStateOf(System.currentTimeMillis()) }; var error by remember { mutableStateOf("") }; val context = LocalContext.current
    AlertDialog(onDismissRequest = onDismiss, title = { Text(tr("Record payment", "പേയ്മെന്റ് രേഖപ്പെടുത്തുക")) }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("${tr("Original amount", "ആദ്യ തുക")}: ${money(debt.amountPaise)}"); OutlinedTextField(amount, { amount = it }, label = { Text(tr("Payment amount (₹)", "പേയ്മെന്റ് തുക (₹)")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)); OutlinedButton(onClick = { val c = Calendar.getInstance(); DatePickerDialog(context, { _, y, m, d -> c.set(y,m,d); date = c.timeInMillis }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show() }) { Text("${tr("Payment date", "പേയ്മെന്റ് തീയതി")}: ${dateText(date)}") }; Text(tr("Payment method", "പേയ്മെന്റ് രീതി")); Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { listOf("Cash", "Bank", "UPI", "Other").forEach { FilterChip(selected = method == it, onClick = { method = it }, label = { Text(it) }) } }; OutlinedTextField(note, { note = it }, label = { Text(tr("Notes (optional)", "കുറിപ്പുകൾ (ഐച്ഛികം)")) }); if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error) } }, confirmButton = { TextButton(onClick = { val v = parsePaise(amount); if (v == null || v <= 0) error = tr("Enter a valid payment amount", "ശരിയായ പേയ്മെന്റ് തുക നൽകുക") else onSave(v, date, method, note) }) { Text(tr("Save payment", "പേയ്മെന്റ് സേവ് ചെയ്യുക")) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(tr("Cancel", "റദ്ദാക്കുക")) } })
}

@Composable private fun StatementDialog(message: String, tr: (String,String)->String, onChange: (String)->Unit, onDismiss: ()->Unit, onShare: ()->Unit, onWhatsApp: ()->Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(tr("Preview statement", "കണക്ക് പരിശോധിക്കുക")) }, text = { OutlinedTextField(message, onChange, modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp), minLines = 7) }, confirmButton = { TextButton(onClick = onWhatsApp) { Icon(Icons.Default.Send, null); Text(" WhatsApp") } }, dismissButton = { Row { TextButton(onClick = onShare) { Text(tr("Share", "പങ്കിടുക")) }; TextButton(onClick = onDismiss) { Text(tr("Close", "അടയ്ക്കുക")) } } })
}
