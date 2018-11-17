package ohtu.verkkokauppa;

import org.junit.Test;
import static org.mockito.Mockito.*;

public class KauppaTest {

    // begin direct copypasta from the materials
    @Test
    public void ostoksenPaaytyttyaPankinMetodiaTilisiirtoKutsutaan() {
        // luodaan ensin mock-oliot
        Pankki pankki = mock(Pankki.class);

        Viitegeneraattori viite = mock(Viitegeneraattori.class);
        // määritellään että viitegeneraattori palauttaa viitten 42
        when(viite.uusi()).thenReturn(42);

        Varasto varasto = mock(Varasto.class);
        // määritellään että tuote numero 1 on maito jonka hinta on 5 ja saldo 10
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        // sitten testattava kauppa
        Kauppa k = new Kauppa(varasto, pankki, viite);

        // tehdään ostokset
        k.aloitaAsiointi();
        k.lisaaKoriin(1);     // ostetaan tuotetta numero 1 eli maitoa
        k.tilimaksu("pekka", "12345");

        // sitten suoritetaan varmistus, että pankin metodia tilisiirto on kutsuttu
        verify(pankki).tilisiirto(anyString(), anyInt(), anyString(), anyString(), anyInt());
        // toistaiseksi ei välitetty kutsussa käytetyistä parametreista
    }
    // end direct copypasta from the materials

    @Test
    public void ostoksenPaaytyttyaPankinMetodiaTilisiirtoKutsutaanOikeiParametreilla() {
        Pankki pankki = mock(Pankki.class);

        Viitegeneraattori viite = mock(Viitegeneraattori.class);
        when(viite.uusi()).thenReturn(42);

        Varasto varasto = mock(Varasto.class);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        Kauppa k = new Kauppa(varasto, pankki, viite);

        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.tilimaksu("pekka", "12345");

        verify(pankki).tilisiirto(
            eq("pekka"),
            anyInt(),
            eq("12345"),
            anyString(),
            eq(5)
        );
    }

    @Test
    public void kahdenEriTuotteenOstoksenPaaytyttyaPankinMetodiaTilisiirtoKutsutaanOikein() {
        Pankki pankki = mock(Pankki.class);

        Viitegeneraattori viite = mock(Viitegeneraattori.class);
        when(viite.uusi()).thenReturn(43);

        Varasto varasto = mock(Varasto.class);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.saldo(2)).thenReturn(5);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "kahvi", 8));

        Kauppa k = new Kauppa(varasto, pankki, viite);

        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.lisaaKoriin(2);
        k.tilimaksu("arto", "54321");

        verify(pankki).tilisiirto(
            eq("arto"),
            anyInt(),
            eq("54321"),
            anyString(),
            eq(5 + 8)
        );
    }

    @Test
    public void kahdenSamanTuotteenOstoksenPaaytyttyaPankinMetodiaTilisiirtoKutsutaanOikein() {
        Pankki pankki = mock(Pankki.class);

        Viitegeneraattori viite = mock(Viitegeneraattori.class);
        when(viite.uusi()).thenReturn(43);

        Varasto varasto = mock(Varasto.class);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        Kauppa k = new Kauppa(varasto, pankki, viite);

        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.lisaaKoriin(1);
        k.tilimaksu("arto", "54321");

        verify(pankki).tilisiirto(
            eq("arto"),
            anyInt(),
            eq("54321"),
            anyString(),
            eq(5 * 2)
        );
    }

    @Test
    public void loppuOlevanTuotteenLisattyaOstoksenPaaytyttyaPankinMetodiaTilisiirtoKutsutaanOikein() {
        Pankki pankki = mock(Pankki.class);

        Viitegeneraattori viite = mock(Viitegeneraattori.class);
        when(viite.uusi()).thenReturn(43);

        Varasto varasto = mock(Varasto.class);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.saldo(2)).thenReturn(0);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "sohva", 100));

        Kauppa k = new Kauppa(varasto, pankki, viite);

        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.lisaaKoriin(2);
        k.tilimaksu("arto", "54321");

        verify(pankki).tilisiirto(
            eq("arto"),
            anyInt(),
            eq("54321"),
            anyString(),
            eq(5)
        );
    }

    @Test
    public void aloitaAsiointiNollaaEdellisetOstokset() {
        Pankki pankki = mock(Pankki.class);

        Viitegeneraattori viite = mock(Viitegeneraattori.class);
        when(viite.uusi()).thenReturn(44);

        Varasto varasto = mock(Varasto.class);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        Kauppa k = new Kauppa(varasto, pankki, viite);

        k.aloitaAsiointi();
        k.lisaaKoriin(1);

        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.tilimaksu("arto", "54321");

        verify(pankki).tilisiirto(
            anyString(),
            anyInt(),
            anyString(),
            anyString(),
            eq(5)
        );
    }

    @Test
    public void poistaKoristaMetodillaPoistettuTuoteEiPaadyMaksettavaksi() {
        Pankki pankki = mock(Pankki.class);

        Viitegeneraattori viite = mock(Viitegeneraattori.class);
        when(viite.uusi()).thenReturn(44);

        Varasto varasto = mock(Varasto.class);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.saldo(2)).thenReturn(10);
        when(varasto.saldo(3)).thenReturn(5);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "kissa", 500));
        when(varasto.haeTuote(3)).thenReturn(new Tuote(3, "kookos", 2));

        Kauppa k = new Kauppa(varasto, pankki, viite);

        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.lisaaKoriin(2);
        k.lisaaKoriin(3);
        k.poistaKorista(2);
        k.tilimaksu("arto", "54321");

        verify(pankki).tilisiirto(
            anyString(),
            anyInt(),
            anyString(),
            anyString(),
            eq(7)
        );
    }

    @Test
    public void poistaKoristaMetodillaPoistettuTuotePalautetaanVarastoon() {
        Pankki pankki = mock(Pankki.class);

        Viitegeneraattori viite = mock(Viitegeneraattori.class);

        Varasto varasto = mock(Varasto.class);
        Tuote testiTuote = new Tuote(1, "maito", 5);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(testiTuote);

        Kauppa k = new Kauppa(varasto, pankki, viite);

        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.poistaKorista(1);

        verify(varasto, times(1)).palautaVarastoon(
            eq(testiTuote)
        );
    }

    @Test
    public void poistaKoristaEiPalautaVarastoonTuotettaJokaEiOleKorissa() {
        Pankki pankki = mock(Pankki.class);
        Viitegeneraattori viite = mock(Viitegeneraattori.class);

        Varasto varasto = mock(Varasto.class);
        Tuote testiTuote = new Tuote(1, "maito", 5);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(testiTuote);

        Kauppa k = new Kauppa(varasto, pankki, viite);

        k.aloitaAsiointi();
        k.lisaaKoriin(2);
        k.poistaKorista(1);

        verify(varasto, times(0)).palautaVarastoon(
            any()
        );
    }
}
