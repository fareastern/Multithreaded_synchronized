import java.util.*;
import java.util.concurrent.*;

public class RobotRouteAnalyzer {
    
    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();

    public static void main(String[] args) throws InterruptedException {

        int routesCount = 1000; // Количество маршрутов / потоков
        int routeLength = 100;  // Длина одного маршрута
        String letters = "RLRFR"; // Буквы для генерации маршрута

        // Создаем пул потоков
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        // Список для хранения всех задач, чтобы потом дождаться их завершения, future<?> - так как нам не важен тип результата
        List<Future<?>> tasks = new ArrayList<>();

        // Создаём и отправляем задачи на выполнение
        for (int i = 0; i < routesCount; i++) {
            // Лямбда описывает, что нужно сделать потоку
            Future<?> task = executor.submit(() -> {
                // Генерируем случайный маршрут
                String route = generateRoute(letters, routeLength);

                // Считаем количество букв 'R'
                int countR = 0;
                for (char c : route.toCharArray()) { // Проходим по всем символам
                    if (c == 'R') {
                        countR++; // Увеличиваем счётчик
                    }
                }

                // Выводим результат для каждого маршрута
                System.out.println("Маршрут: " + route + " | Количество 'R': " + countR);

                // Обновляем общую мапу синхронизировано
                synchronized (sizeToFreq) {
                    // Получаем текущее значение или 0, если такого ключа ещё нет, чтобы посчитать, сколько раз уже встречалось данное количество R
                    sizeToFreq.put(countR, sizeToFreq.getOrDefault(countR, 0) + 1);
                }
            });
            tasks.add(task); // Сохраняем задачу в список
        }

        // Ждём завершения всех задач
        for (Future<?> task : tasks) {
            try {
                task.get(); // Ожидание окончания конкретной задачи
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        // Завершаем работу пула потоков
        executor.shutdown();

        // После выполнения всех потоков находим максимальную частоту встречаемости
        Map.Entry<Integer, Integer> maxEntry = sizeToFreq.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue()) // Сравнение по количеству повторений
                .orElse(null);

        if (maxEntry != null) {
            System.out.println("\nСамое частое количество повторений "
                    + maxEntry.getKey() + " (встретилось " + maxEntry.getValue() + " раз)");

            System.out.println("Другие размеры:");
            sizeToFreq.entrySet().stream()
                    .filter(e -> !e.equals(maxEntry)) // Исключаем самый частый
                    .sorted(Map.Entry.comparingByKey()) // Сортируем по ключу (кол-ву 'R')
                    .forEach(e -> System.out.println("- " + e.getKey() + " (" + e.getValue() + " раз)"));
        }
    }

    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();

        for (int i = 0; i < length; i++) {
            // Выбираем случайный символ из letters
            route.append(letters.charAt(random.nextInt(letters.length())));
        }

        return route.toString();
    }
}
