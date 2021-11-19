# Проект "Облачное хранилище"

Данное приложение, написанное на spring boot, представляет собой REST-сервис для работы с файлами. Приложение предоставляет функции загрузки, скачивания, удаления, переименования файлов, вывода списка файлов, хранимых на жёстком диске. 

Доступ к приложению имеют только авторизованные пользователи. 
Для хранения информации о пользователях и данных в проекте используется база данных, управляемая посредством MySql и Liquibase.
Авторизация осуществляется при использовании jwt токена, значение которого передается в заголовке каждого запроса клиента.
Чтобы получить jwt токен, необходимо пройти аутентификацию. Введенные данные логина и пароля сравниваются с данными, хранимыми в базе данных.

Сервер обрабатывает и отправляет сообщения в соответствии со спецификацией ФРОНТА [yaml](https://github.com/netology-code/jd-homeworks/blob/master/diploma/CloudServiceSpecification.yaml)


Запуск приложения осуществляется с использованием docker и файла docker-compose.yml
Во время запуска будет инициализирована база данных с информацией о пользователях и их файлах. На данный момент в таблице 4 пользователя. Данные любого из них можно использовать для входа в систему:

login			passrord

evgenius@gmail.com	password1
pavlik@gmail.com	password2
olechka@mail.ru		password3
ishka@yandex.ru		password4


В структуре проекта можно выделить несколько основных логических блоков:
* controller
* config
* service
* repository
* security

## Controller

В данном пакете содержатся Rest контроллеры. Эти классы принимают запросы пользователей, делегируют их обработку сервисам, а также отправляют клиенту обратно результат обработки. 

## Config

Данный пакет содержит класс, определяющий конфигурацию spring boot security.

## Repository

Пакет, содержажий интерфейсы по работе с таблицами пользователей (таблица users) и данных (таблица files).

Таблица users хранит информацию о пользователе (логин, пароль, имя, фамилия, когда был зарегистрирован).
Таблица files хранит информацию о файлах (название, размер, дату создания и изменения, статус, а также id пользователя, загрузившего этот файл).

Для каждой базы данных созданы интерфейсы, наследники JpaRepository, содержание методы для работы с базами данных (UserRepository, FileRepository).

## Service

Интерфейсы по работе с базами данных расширяют интерфейсы-сервисы UserService и FileService.
Наследником интерфейсов-сервисов UserService и FileService является интерфейс CloudDBService. Он содержит методы для работы с объединенной базой данных. Методы для выполнения основных операций по работе с базой-облаком (загрузка файла, скачивание, переименование, удаление, скачивание списка файлов).
Данный интерфейс имплементирует класс-сервис CloudDBServiceImpl.

Сами файлы хранятся на жёстком диске. И за операции, связанные с их загрузкой, выгрузкой, переименованием и удалением отвечают интерфейс CloudFileService и имплементирующий его класс-сервис CloudFileServiceImpl.

CloudStorageService - интерфейс, содержащий методы по работе с облачным хранилищем. Его имплементирует класс-сервис CloudStorageServiceImpl с конкретной реализацией каждого метода. Этот класс объединяет в себе сервисы CloudDbService и CloudFileService, и является основным в обработке запросов контроллера.

AssistantService - интерфейс, содержащий методы класса-помощника, реализующего прослойку между CloudStorageService и контроллерами.


## Security

В данном пакете содержатся все классы и сервисы для работы с токеном. Здесь осуществляется генерация токена при успешной аутентификации пользователя,  его проверка при каждом запросе. Полсле того, как пользователь разлогинился, его токен отправляется в черный список, который представляет из себя таблицу blacklist в нашей базе данных. 
