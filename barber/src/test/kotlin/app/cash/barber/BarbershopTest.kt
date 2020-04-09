package app.cash.barber

import app.cash.barber.examples.RecipientReceipt
import app.cash.barber.examples.SenderReceipt
import app.cash.barber.examples.TransactionalEmailDocument
import app.cash.barber.examples.TransactionalSmsDocument
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_CA
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_GB
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_US
import app.cash.barber.examples.recipientReceiptSmsEmailDocumentTemplateEN_US
import app.cash.barber.examples.senderReceiptEmailDocumentTemplateEN_US
import app.cash.barber.models.BarberKey
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BarbershopTest {
  @Test
  fun `getBarber happy path`() {
    val barbershop = BarbershopBuilder()
      .installDocument<TransactionalSmsDocument>()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
      .build()
    val barber = barbershop.getBarber<RecipientReceipt, TransactionalSmsDocument>()
    assertEquals("class app.cash.barber.RealBarber", barber::class.toString())
  }

  @Test
  fun `getAllBarbers happy path`() {
    val barbershop = BarbershopBuilder()
      .installDocument<TransactionalSmsDocument>()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_CA)
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_GB)
      .build()
    val barbers = barbershop.getAllBarbers()
    assertEquals(1, barbers.size)
    assertThat(barbers.keys).contains(
      BarberKey(RecipientReceipt::class, TransactionalSmsDocument::class))
  }

  @Test
  fun `getBarber fail on non-target Document, with Document installed`() {
    val barber = BarbershopBuilder()
      .installDocumentTemplate<SenderReceipt>(senderReceiptEmailDocumentTemplateEN_US)
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
      .installDocument<TransactionalSmsDocument>()
      .installDocument<TransactionalEmailDocument>()
      .build()

    val exception = assertFailsWith<BarberException> {
      barber.getBarber<RecipientReceipt, TransactionalEmailDocument>()
    }
    assertEquals(
      """
        |Errors
        |1) Failed to get Barber<class app.cash.barber.examples.RecipientReceipt, class app.cash.barber.examples.TransactionalEmailDocument>
        |Requested Document [class app.cash.barber.examples.TransactionalEmailDocument] is installed
        |Requested DocumentData [class app.cash.barber.examples.RecipientReceipt] is installed
        |DocumentTemplate with source=[class app.cash.barber.examples.RecipientReceipt] does not have target=[class app.cash.barber.examples.TransactionalEmailDocument]
        |
      """.trimMargin(),
      exception.toString())
  }

  @Test
  fun `getBarber fail on non-target Document, with Document not installed`() {
    val barber = BarbershopBuilder()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
      .installDocument<TransactionalSmsDocument>()
      .build()

    val exception = assertFailsWith<BarberException> {
      barber.getBarber<RecipientReceipt, TransactionalEmailDocument>()
    }
    assertEquals(
      """
        |Errors
        |1) Failed to get Barber<class app.cash.barber.examples.RecipientReceipt, class app.cash.barber.examples.TransactionalEmailDocument>
        |Document [class app.cash.barber.examples.TransactionalEmailDocument] is not installed in Barbershop
        |
      """.trimMargin(),
      exception.toString())
  }

  @Test
  fun `getTargetDocuments happy path`() {
    val barbershop = BarbershopBuilder()
      .installDocument<TransactionalSmsDocument>()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
      .build()
    val supportedDocuments = barbershop.getTargetDocuments<RecipientReceipt>()
    assertThat(supportedDocuments).containsOnly(TransactionalSmsDocument::class)
  }

  @Test
  fun `getTargetDocuments multiple supported documents`() {
    val barbershop = BarbershopBuilder()
      .installDocument<TransactionalSmsDocument>()
      .installDocument<TransactionalEmailDocument>()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsEmailDocumentTemplateEN_US)
      .build()
    val supportedDocuments = barbershop.getTargetDocuments<RecipientReceipt>()
    assertThat(supportedDocuments).containsOnly(
      TransactionalSmsDocument::class,
      TransactionalEmailDocument::class
    )
  }

  @Test
  fun `getTargetDocuments no supported documents`() {
    val barbershop = BarbershopBuilder()
      .installDocument<TransactionalSmsDocument>()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
      .build()
    val supportedDocuments = barbershop.getTargetDocuments<SenderReceipt>()
    assertThat(supportedDocuments).isEmpty()
  }
}
