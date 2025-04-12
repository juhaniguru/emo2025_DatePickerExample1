import com.example.projecttemplateexample.NetworkChecker
import com.example.projecttemplateexample.UserDataService
import com.example.projecttemplateexample.models.UserDto
import com.example.projecttemplateexample.vm.UsersViewModel
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class FakeNetworkChecker : NetworkChecker {
    override fun isNetworkAvailable(): Boolean {
        return true
    }
}

class MockUsersService : UserDataService {
    // implementoidaan getUsers-funktio
    // oikeasssa UsersSevicessähän tämä tekee rajapintahaun jsontypicoden palvelimelle
    // nyt se palauttaa listan, jossa on kaksi käyttäjää
    override suspend fun getUsers(): List<UserDto> {

        return listOf(
            UserDto(
                1, "Mock User 1", "mock1@example.com",
                email = "mock1@example.com"
            ),
            UserDto(
                2, "Mock User 2", "mock2@example.com",
                email = "mock2@example.com"
            )
        )
    }
}

// varsinainen testiluokka
class UsersViewModelTest {

    private lateinit var viewModel: UsersViewModel
    private lateinit var service: UserDataService
    private lateinit var networkChecker: FakeNetworkChecker


    // tarvitaan optinia coroutineiden testausta varten
    // setup funktio ajetaan ennen testeje
    @OptIn(ExperimentalCoroutinesApi::class)

    @Before
    fun setup() {
        // Tällä viewmodelissa käytettävä coroutineScope (ViewModelScope)
        // saadaan suoritettua TestThreadissa
        // testit on hyvä suorittaa emulaattorin ulkopuolella (Ei siis mainthreadissa)
        Dispatchers.setMain(Dispatchers.Unconfined)
        // tehdään isntanssi mockupservicesta testiä varten
        service = MockUsersService() // Or use Mockito mock
        networkChecker = FakeNetworkChecker()
        // tehdään viewmodelista instanssi ja annetaan service dependncyna
        viewModel = UsersViewModel(service, networkChecker)
    }


    // tässä voidaan poistaa testien tulosket
    // jotta jokainen testikierros aloitetaan puhtaalta pöydältä
    // (aiempiten testien tulokset eivät saa vaikuttaa uusiin testeihin)
    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        // tämä rivi palauttaa mainthreadin ennalleen testien jälkeen
        Dispatchers.resetMain()
    }

    // tässä on varsinainen testicase
    @Test
    fun `getUsers should update state with mock data`(): Unit = runTest {

        // odotetaan, että tällaienn lista käyttäjiä pitäisi olla viewmodelin statessa
        val expectedUsers = listOf(
            UserDto(
                1, "Mock User 1", "mock1@example.com",
                email = "mock1@example.com"
            ),
            UserDto(
                2, "Mock User 2", "mock2@example.com",
                email = "mock2@example.com"
            )
        )

        // testataan tässä, onko viewmodelissa samat käyttäjät kuin pitäisi olla
        assertEquals(expectedUsers, viewModel.state.value.users)
    }
}